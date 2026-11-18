package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.Partitura;
import br.cefetmg.ocva.dto.EventoRequestDTO;
import br.cefetmg.ocva.dto.EventoResponseDTO;
import br.cefetmg.ocva.repository.EventoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;
import br.cefetmg.ocva.repository.PartituraRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final MusicoRepository musicoRepository;
    private final PartituraRepository partituraRepository;

    public EventoService(EventoRepository eventoRepository, MusicoRepository musicoRepository,
            PartituraRepository partituraRepository) {
        this.eventoRepository = eventoRepository;
        this.musicoRepository = musicoRepository;
        this.partituraRepository = partituraRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listar() {
        return eventoRepository.findAll().stream().map(EventoResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public EventoResponseDTO buscarPorId(Long id) {
        return eventoRepository.findById(id).map(EventoResponseDTO::new).orElse(null);
    }

    @Transactional
    public EventoResponseDTO inserir(EventoRequestDTO dto) {
        validarData(dto.getData(), null);

        Evento evento = converter(dto);
        evento.setId(null);
        evento.setMusicos(resolverMusicos(dto.getMusicoIds()));
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
        existente.setMusicos(resolverMusicos(dto.getMusicoIds()));
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
        evento.setPartituras(new ArrayList<>());
        eventoRepository.saveAndFlush(evento);
        eventoRepository.delete(evento);
        return new EventoResponseDTO(evento);
    }

    private List<Musico> resolverMusicos(List<Long> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return ids.stream()
                .map(id -> {
                    if (id == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Todo músico deve possuir id");
                    }
                    return musicoRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Músico com id: " + id + " não encontrado"));
                })
                .collect(Collectors.toList());
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
