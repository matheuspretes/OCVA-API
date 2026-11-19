package br.cefetmg.ocva.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.cefetmg.ocva.dto.EventoMarcadoRequestDTO;
import br.cefetmg.ocva.dto.EventoMarcadoResponseDTO;
import br.cefetmg.ocva.service.EventoMarcadoService;

/**
 * Eventos que ainda vão acontecer. Depois da data viram registros em /api/v1/eventos.
 */
@RestController
@RequestMapping("/api/v1/eventos-marcados")
@CrossOrigin(origins = "*")
public class EventoMarcadoController {

    private final EventoMarcadoService service;

    public EventoMarcadoController(EventoMarcadoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventoMarcadoResponseDTO> listar() {
        return service.listar();
    }

    // O evento marcado mais próximo (204 se não houver nenhum)
    @GetMapping("/proximo")
    public ResponseEntity<EventoMarcadoResponseDTO> proximo() {
        EventoMarcadoResponseDTO proximo = service.proximo();
        return proximo == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(proximo);
    }

    @GetMapping("/{id}")
    public EventoMarcadoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public EventoMarcadoResponseDTO inserir(@RequestBody EventoMarcadoRequestDTO dto) {
        return service.inserir(dto);
    }

    @PutMapping
    public EventoMarcadoResponseDTO atualizar(@RequestBody EventoMarcadoRequestDTO dto) {
        return service.atualizar(dto);
    }

    @DeleteMapping("/{id}")
    public EventoMarcadoResponseDTO excluir(@PathVariable Long id) {
        return service.excluir(id);
    }

    @PutMapping("/{eventoId}/interesse/{musicoId}")
    public EventoMarcadoResponseDTO responder(@PathVariable Long eventoId, @PathVariable Long musicoId,
            @RequestParam boolean interessado) {
        return service.responder(eventoId, musicoId, interessado);
    }

    @DeleteMapping("/{eventoId}/interesse/{musicoId}")
    public EventoMarcadoResponseDTO removerResposta(@PathVariable Long eventoId, @PathVariable Long musicoId) {
        return service.removerResposta(eventoId, musicoId);
    }
}
