package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.dto.EnsaioRequestDTO;
import br.cefetmg.ocva.dto.EnsaioResponseDTO;
import br.cefetmg.ocva.repository.EnsaioRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

@Service
public class EnsaioService {

    private final EnsaioRepository ensaioRepository;
    private final MusicoRepository musicoRepository;

    public EnsaioService(EnsaioRepository ensaioRepository, MusicoRepository musicoRepository) {
        this.ensaioRepository = ensaioRepository;
        this.musicoRepository = musicoRepository;
    }

    @Transactional(readOnly = true)
    public List<EnsaioResponseDTO> listar() {
        return ensaioRepository.findAll().stream().map(EnsaioResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public EnsaioResponseDTO buscarPorId(Long id) {
        return ensaioRepository.findById(id).map(EnsaioResponseDTO::new).orElse(null);
    }

    @Transactional
    public EnsaioResponseDTO inserir(EnsaioRequestDTO dto) {
        Ensaio ensaio = converter(dto);
        ensaio.setId(null);
        ensaio.setMusicos(resolverMusicos(dto.getMusicoIds()));
        ensaio.setCriador(dto.getCriadorId() == null ? null : buscarMusico(dto.getCriadorId()));
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
        ensaio.setData(dto.getData());
        ensaio.setDescricao(dto.getDescricao());
        ensaio.setTitulo(dto.getTitulo());
        ensaio.setMusicos(resolverMusicos(dto.getMusicoIds()));
        ensaio.setCriador(dto.getCriadorId() == null ? null : buscarMusico(dto.getCriadorId()));
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

        boolean jaMarcado = (ensaio.getPresencas() != null && ensaio.getPresencas().stream()
                .anyMatch(item -> item.getId().equals(musicoId)))
                || (ensaio.getFaltas() != null && ensaio.getFaltas().stream()
                .anyMatch(item -> item.getId().equals(musicoId)));
        if (jaMarcado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A presença deste músico já foi marcada");
        }

        if (ensaio.getPresencas() == null) {
            ensaio.setPresencas(new ArrayList<>());
        }
        if (ensaio.getFaltas() == null) {
            ensaio.setFaltas(new ArrayList<>());
        }
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

    private List<Musico> resolverMusicos(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream().map(this::buscarMusico).toList();
    }

    private Ensaio converter(EnsaioRequestDTO dto) {
        Ensaio ensaio = new Ensaio();
        ensaio.setData(dto.getData());
        ensaio.setDescricao(dto.getDescricao());
        ensaio.setTitulo(dto.getTitulo());
        return ensaio;
    }
}
