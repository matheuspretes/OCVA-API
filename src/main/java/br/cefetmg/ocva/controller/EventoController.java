package br.cefetmg.ocva.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.dto.EventoRequestDTO;
import br.cefetmg.ocva.dto.EventoResponseDTO;
import br.cefetmg.ocva.service.EventoService;

@RestController
@RequestMapping("/api/v1/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public EventoResponseDTO inserir(@RequestBody EventoRequestDTO dto) {
        return service.inserir(dto);
    }

    @PutMapping
    public EventoResponseDTO atualizar(@RequestBody EventoRequestDTO dto) {
        return service.atualizar(dto);
    }

    @DeleteMapping("/{id}")
    public EventoResponseDTO excluir(@PathVariable long id) {
        return service.excluir(id);
    }
}
