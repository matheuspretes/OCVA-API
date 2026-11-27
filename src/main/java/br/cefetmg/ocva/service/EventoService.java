package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.Partitura;
import br.cefetmg.ocva.dto.EventoRequestDTO;
import br.cefetmg.ocva.dto.EventoResponseDTO;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.EnsaioRepository;
import br.cefetmg.ocva.repository.EventoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;
import br.cefetmg.ocva.repository.PartituraRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final MusicoRepository musicoRepository;
    private final PartituraRepository partituraRepository;
    private final EnsaioRepository ensaioRepository;
    private final EncontroRepository encontroRepository;
    private final AtualizadorAgenda atualizador;
    private final AgendaReferencias referencias;

    public EventoService(EventoRepository eventoRepository, MusicoRepository musicoRepository,
            PartituraRepository partituraRepository, EnsaioRepository ensaioRepository,
            EncontroRepository encontroRepository, AtualizadorAgenda atualizador, AgendaReferencias referencias) {
        this.eventoRepository = eventoRepository;
        this.musicoRepository = musicoRepository;
        this.partituraRepository = partituraRepository;
        this.ensaioRepository = ensaioRepository;
        this.encontroRepository = encontroRepository;
        this.atualizador = atualizador;
        this.referencias = referencias;
    }

    @Transactional
    public List<EventoResponseDTO> listar() {
        atualizador.atualizar();
        return eventoRepository.findAll().stream().map(EventoResponseDTO::new).toList();
    }

    @Transactional
    public EventoResponseDTO buscarPorId(Long id) {
        atualizador.atualizar();
        return eventoRepository.findById(id).map(EventoResponseDTO::new).orElse(null);
    }

    @Transactional
    public EventoResponseDTO inserir(EventoRequestDTO dto) {
        validarData(dto.getData(), null);

        Evento evento = converter(dto);
        evento.setId(null);
        evento.setMaestro(referencias.buscarMaestro(dto.getMaestroId(), null));
        evento.setMusicos(referencias.buscarMusicos(dto.getMusicoIds(), null));
        evento.setInstrumentos(AgendaValidador.validarInstrumentos(dto.getInstrumentos()));
        evento.setPartituras(resolverPartituras(dto.getPartituraIds()));
        return new EventoResponseDTO(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponseDTO atualizar(EventoRequestDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id é obrigatório");
        }

        Evento existente = eventoRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento com id: " + dto.getId() + " não encontrado"));

        // Só valida a data se ela foi alterada, assim eventos antigos ainda podem ser editados
        if (!existente.getData().equals(dto.getData())) {
            validarData(dto.getData(), existente.getId());
        }

        existente.setData(dto.getData());
        existente.setDescricao(dto.getDescricao());
        existente.setTitulo(dto.getTitulo());
        existente.setLocal(dto.getLocal());
        existente.setBanner(dto.getBanner());
        existente.setMaestro(referencias.buscarMaestro(dto.getMaestroId(), existente.getMaestro()));
        existente.setMusicos(referencias.buscarMusicos(dto.getMusicoIds(), existente.getMusicos()));
        existente.setInstrumentos(AgendaValidador.validarInstrumentos(dto.getInstrumentos()));
        existente.setPartituras(resolverPartituras(dto.getPartituraIds()));
        return new EventoResponseDTO(eventoRepository.save(existente));
    }

    @Transactional
    public EventoResponseDTO excluir(long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento com id: " + id + " não encontrado"));
        evento.setMusicos(new ArrayList<>());
        evento.setPresencas(new ArrayList<>());
        evento.setFaltas(new ArrayList<>());
        evento.setPartituras(new ArrayList<>());

        // Os ensaios do evento ficam sem evento
        for (Ensaio ensaio : ensaioRepository.findByEventoId(id)) {
            ensaio.setEvento(null);
        }
        for (Encontro encontro : encontroRepository.findByEventoId(id)) {
            encontro.setEvento(null);
        }
        eventoRepository.saveAndFlush(evento);
        eventoRepository.delete(evento);
        return new EventoResponseDTO(evento);
    }

    /**
     * Marca presença (presente = true) ou falta. Se o músico já tinha outra
     * marcação, ela é trocada.
     */
    @Transactional
    public EventoResponseDTO marcarPresenca(Long eventoId, Long musicoId, boolean presente) {
        Evento evento = buscarEvento(eventoId);
        Musico musico = buscarMusico(musicoId);
        limparPresenca(evento, musicoId);
        (presente ? evento.getPresencas() : evento.getFaltas()).add(musico);
        return new EventoResponseDTO(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponseDTO removerPresenca(Long eventoId, Long musicoId) {
        Evento evento = buscarEvento(eventoId);
        buscarMusico(musicoId);
        limparPresenca(evento, musicoId);
        return new EventoResponseDTO(eventoRepository.save(evento));
    }

    private void limparPresenca(Evento evento, Long musicoId) {
        if (evento.getPresencas() == null) {
            evento.setPresencas(new ArrayList<>());
        }
        if (evento.getFaltas() == null) {
            evento.setFaltas(new ArrayList<>());
        }
        evento.getPresencas().removeIf(m -> musicoId.equals(m.getId()));
        evento.getFaltas().removeIf(m -> musicoId.equals(m.getId()));
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
    }

    private Musico buscarMusico(Long id) {
        return musicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Músico não encontrado"));
    }

    private void validarData(String data, Long eventoId) {
        AgendaValidador.validarData(data);

        // No cadastro o evento ainda não tem id, então usamos 0 (nenhum evento tem esse id)
        Long idIgnorado = eventoId == null ? 0L : eventoId;
        if (eventoRepository.existsByDataStartingWithAndIdNot(AgendaValidador.pegarDia(data), idIgnorado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um evento cadastrado neste dia.");
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

    private Evento converter(EventoRequestDTO dto) {
        Evento evento = new Evento();
        evento.setData(dto.getData());
        evento.setDescricao(dto.getDescricao());
        evento.setTitulo(dto.getTitulo());
        evento.setLocal(dto.getLocal());
        evento.setBanner(dto.getBanner());
        return evento;
    }
}
