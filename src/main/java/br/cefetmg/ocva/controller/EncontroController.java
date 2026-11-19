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

import br.cefetmg.ocva.dto.EncontroRequestDTO;
import br.cefetmg.ocva.dto.EncontroResponseDTO;
import br.cefetmg.ocva.dto.TipoEncontroRequestDTO;
import br.cefetmg.ocva.model.TipoEncontro;
import br.cefetmg.ocva.service.EncontroService;

@RestController
@RequestMapping("/api/v1/encontros")
@CrossOrigin(origins = "*")
public class EncontroController {

    private final EncontroService service;

    public EncontroController(EncontroService service) {
        this.service = service;
    }

    // Encontros de hoje em diante, ordenados pelo mais próximo
    @GetMapping
    public List<EncontroResponseDTO> listar() {
        return service.listar();
    }

    // O encontro mais próximo (204 se não houver nenhum)
    @GetMapping("/proximo")
    public ResponseEntity<EncontroResponseDTO> proximo() {
        EncontroResponseDTO proximo = service.proximo();
        return proximo == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(proximo);
    }

    @GetMapping("/tipos")
    public List<TipoEncontro> listarTipos() {
        return service.listarTipos();
    }

    @PostMapping("/tipos")
    public TipoEncontro inserirTipo(@RequestBody TipoEncontroRequestDTO dto) {
        return service.inserirTipo(dto.getNome());
    }

    @GetMapping("/{id}")
    public EncontroResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public EncontroResponseDTO inserir(@RequestBody EncontroRequestDTO dto) {
        return service.inserir(dto);
    }

    @PutMapping
    public EncontroResponseDTO atualizar(@RequestBody EncontroRequestDTO dto) {
        return service.atualizar(dto);
    }

    @DeleteMapping("/{id}")
    public EncontroResponseDTO excluir(@PathVariable Long id) {
        return service.excluir(id);
    }

    @PutMapping("/{encontroId}/interesse/{musicoId}")
    public EncontroResponseDTO responder(@PathVariable Long encontroId, @PathVariable Long musicoId,
            @RequestParam boolean interessado) {
        return service.responder(encontroId, musicoId, interessado);
    }

    @DeleteMapping("/{encontroId}/interesse/{musicoId}")
    public EncontroResponseDTO removerResposta(@PathVariable Long encontroId, @PathVariable Long musicoId) {
        return service.removerResposta(encontroId, musicoId);
    }
}
