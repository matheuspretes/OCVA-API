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
import br.cefetmg.ocva.dto.EventoRequestDTO;
import br.cefetmg.ocva.dto.EventoResponseDTO;
import br.cefetmg.ocva.repository.EventoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final MusicoRepository musicoRepository;

    public EventoService(EventoRepository eventoRepository, MusicoRepository musicoRepository) {
        this.eventoRepository = eventoRepository;
        this.musicoRepository = musicoRepository;
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
        Evento evento = converter(dto);
        evento.setId(null);
        evento.setMusicos(resolverMusicos(dto.getMusicoIds()));
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

        existente.setData(dto.getData());
        existente.setDescricao(dto.getDescricao());
        existente.setTitulo(dto.getTitulo());
        existente.setLocal(dto.getLocal());
        existente.setBanner(dto.getBanner());
        existente.setMusicos(resolverMusicos(dto.getMusicoIds()));
        return new EventoResponseDTO(eventoRepository.save(existente));
    }

    @Transactional
    public EventoResponseDTO excluir(long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento com id: " + id + " não encontrado"));
        evento.setMusicos(new ArrayList<>());
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
