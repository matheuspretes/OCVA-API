package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.Partitura;
import br.cefetmg.ocva.dto.EnsaioRequestDTO;
import br.cefetmg.ocva.dto.EnsaioResponseDTO;
import br.cefetmg.ocva.repository.EnsaioRepository;
import br.cefetmg.ocva.repository.MusicoRepository;
import br.cefetmg.ocva.repository.PartituraRepository;

@Service
public class EnsaioService {

    private final EnsaioRepository ensaioRepository;
    private final MusicoRepository musicoRepository;
    private final PartituraRepository partituraRepository;
    private final AtualizadorAgenda atualizador;
    private final AgendaReferencias referencias;

    public EnsaioService(EnsaioRepository ensaioRepository, MusicoRepository musicoRepository,
            PartituraRepository partituraRepository, AtualizadorAgenda atualizador, AgendaReferencias referencias) {
        this.ensaioRepository = ensaioRepository;
        this.musicoRepository = musicoRepository;
        this.partituraRepository = partituraRepository;
        this.atualizador = atualizador;
        this.referencias = referencias;
    }

    @Transactional
    public List<EnsaioResponseDTO> listar() {
        atualizador.atualizar();
        return ensaioRepository.findAll().stream().map(EnsaioResponseDTO::new).toList();
    }

    @Transactional
    public EnsaioResponseDTO buscarPorId(Long id) {
        atualizador.atualizar();
        return ensaioRepository.findById(id).map(EnsaioResponseDTO::new).orElse(null);
    }

    @Transactional
    public EnsaioResponseDTO inserir(EnsaioRequestDTO dto) {
        validarData(dto.getData(), null);

        Ensaio ensaio = converter(dto);
        ensaio.setId(null);
        ensaio.setMaestro(referencias.buscarMaestro(dto.getMaestroId(), null));
        definirEvento(ensaio, dto);
        ensaio.setMusicos(referencias.buscarMusicos(dto.getMusicoIds(), null));
        // Todos os músicos escalados começam com presença
        ensaio.setPresencas(new ArrayList<>());
        ensaio.setFaltas(new ArrayList<>());
        PresencaPadrao.atualizar(ensaio.getMusicos(), ensaio.getPresencas(), ensaio.getFaltas());
        ensaio.setCriador(dto.getCriadorId() == null ? null : buscarMusico(dto.getCriadorId()));
        ensaio.setInstrumentos(AgendaValidador.validarInstrumentos(dto.getInstrumentos()));
        ensaio.setPartituras(resolverPartituras(dto.getPartituraIds()));
        return new EnsaioResponseDTO(ensaioRepository.save(ensaio));
    }

    @Transactional
    public EnsaioResponseDTO atualizar(EnsaioRequestDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id é obrigatório");
        }
        Ensaio ensaio = ensaioRepository.findById(dto.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Ensaio com id: " + dto.getId() + " não encontrado"));

        // Só valida a data se ela foi alterada, assim ensaios antigos ainda podem ser editados
        if (!ensaio.getData().equals(dto.getData())) {
            validarData(dto.getData(), ensaio.getId());
        }

        ensaio.setData(dto.getData());
        ensaio.setDescricao(dto.getDescricao());
        ensaio.setTitulo(dto.getTitulo());
        ensaio.setMaestro(referencias.buscarMaestro(dto.getMaestroId(), ensaio.getMaestro()));
        definirEvento(ensaio, dto);
        ensaio.setMusicos(referencias.buscarMusicos(dto.getMusicoIds(), ensaio.getMusicos()));
        // Quem entrou na escalação ganha presença; quem saiu perde a marcação
        if (ensaio.getPresencas() == null) {
            ensaio.setPresencas(new ArrayList<>());
        }
        if (ensaio.getFaltas() == null) {
            ensaio.setFaltas(new ArrayList<>());
        }
        PresencaPadrao.atualizar(ensaio.getMusicos(), ensaio.getPresencas(), ensaio.getFaltas());
        if (dto.getCriadorId() != null) {
            ensaio.setCriador(buscarMusico(dto.getCriadorId()));
        }
        ensaio.setInstrumentos(AgendaValidador.validarInstrumentos(dto.getInstrumentos()));
        ensaio.setPartituras(resolverPartituras(dto.getPartituraIds()));
        if (!ensaioRepository.existsById(ensaio.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Ensaio com id: " + ensaio.getId() + " não encontrado");
        }
        return new EnsaioResponseDTO(ensaioRepository.save(ensaio));
    }

    @Transactional
    public EnsaioResponseDTO marcarPresenca(Long ensaioId, Long musicoId, boolean presente) {
        Ensaio ensaio = buscarEnsaio(ensaioId);
        Musico musico = buscarMusico(musicoId);

        // Todo músico já começa com presença, então marcar de novo troca a marcação anterior
        if (ensaio.getPresencas() == null) {
            ensaio.setPresencas(new ArrayList<>());
        }
        if (ensaio.getFaltas() == null) {
            ensaio.setFaltas(new ArrayList<>());
        }
        ensaio.getPresencas().removeIf(item -> musicoId.equals(item.getId()));
        ensaio.getFaltas().removeIf(item -> musicoId.equals(item.getId()));
        (presente ? ensaio.getPresencas() : ensaio.getFaltas()).add(musico);
        return new EnsaioResponseDTO(ensaioRepository.save(ensaio));
    }

    @Transactional
    public EnsaioResponseDTO removerPresenca(Long ensaioId, Long musicoId) {
        Ensaio ensaio = buscarEnsaio(ensaioId);
        buscarMusico(musicoId);
        if (ensaio.getPresencas() != null) {
            ensaio.getPresencas().removeIf(item -> musicoId.equals(item.getId()));
        }
        if (ensaio.getFaltas() != null) {
            ensaio.getFaltas().removeIf(item -> musicoId.equals(item.getId()));
        }
        return new EnsaioResponseDTO(ensaioRepository.save(ensaio));
    }

    @Transactional
    public EnsaioResponseDTO excluir(long id) {
        Ensaio ensaio = buscarEnsaio(id);
        if (ensaio.getMusicos() != null) {
            ensaio.getMusicos().clear();
        }
        if (ensaio.getPresencas() != null) {
            ensaio.getPresencas().clear();
        }
        if (ensaio.getFaltas() != null) {
            ensaio.getFaltas().clear();
        }
        if (ensaio.getPartituras() != null) {
            ensaio.getPartituras().clear();
        }
        ensaioRepository.delete(ensaio);
        return new EnsaioResponseDTO(ensaio);
    }

    private Ensaio buscarEnsaio(Long id) {
        return ensaioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ensaio não encontrado"));
    }

    private Musico buscarMusico(Long id) {
        return musicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Músico não encontrado"));
    }

    /**
     * Ensaios precisam estar ligados a um evento.
     * Registros antigos de outros tipos (ex.: Reunião) não precisam.
     */
    private void definirEvento(Ensaio ensaio, EnsaioRequestDTO dto) {
        boolean ehEnsaio = ensaio.getTipo() == null || ensaio.getTipo().equalsIgnoreCase("ensaio");
        if (ehEnsaio) {
            referencias.validarEventoDoEnsaio(dto.getEventoMarcadoId(), dto.getEventoId());
        }
        ensaio.setEventoMarcado(referencias.buscarEventoMarcado(dto.getEventoMarcadoId()));
        ensaio.setEvento(referencias.buscarEvento(dto.getEventoId()));
    }

    private void validarData(String data, Long ensaioId) {
        AgendaValidador.validarData(data);

        // No cadastro o ensaio ainda não tem id, então usamos 0 (nenhum ensaio tem esse id)
        Long idIgnorado = ensaioId == null ? 0L : ensaioId;
        if (ensaioRepository.existsByDataStartingWithAndIdNot(AgendaValidador.pegarDia(data), idIgnorado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um ensaio cadastrado neste dia.");
        }
    }

    private List<Partitura> resolverPartituras(List<Long> ids) {
        List<Partitura> partituras = new ArrayList<>();
        if (ids == null) {
            return partituras;
        }
        for (Long id : ids) {
            Partitura partitura = partituraRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Partitura com id: " + id + " não encontrada"));
            partituras.add(partitura);
        }
        return partituras;
    }

    private Ensaio converter(EnsaioRequestDTO dto) {
        Ensaio ensaio = new Ensaio();
        ensaio.setData(dto.getData());
        ensaio.setDescricao(dto.getDescricao());
        ensaio.setTitulo(dto.getTitulo());
        return ensaio;
    }
}
