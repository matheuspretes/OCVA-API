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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.dto.EnsaioRequestDTO;
import br.cefetmg.ocva.dto.EnsaioResponseDTO;
import br.cefetmg.ocva.service.EnsaioService;

@RestController
@RequestMapping("/api/v1/ensaios")
@CrossOrigin(origins = "*")
public class EnsaioController {

    private final EnsaioService service;

    public EnsaioController(EnsaioService service) {
        this.service = service;
    }

    @GetMapping
    public List<EnsaioResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EnsaioResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public EnsaioResponseDTO inserir(@RequestBody EnsaioRequestDTO dto) {
        return service.inserir(dto);
    }

    @PutMapping
    public EnsaioResponseDTO atualizar(@RequestBody EnsaioRequestDTO dto) {
        return service.atualizar(dto);
    }

    @PutMapping("/{ensaioId}/presenca/{musicoId}")
    public EnsaioResponseDTO marcarPresenca(@PathVariable Long ensaioId, @PathVariable Long musicoId,
            @RequestParam boolean presente) {
        return service.marcarPresenca(ensaioId, musicoId, presente);
    }

    @DeleteMapping("/{ensaioId}/presenca/{musicoId}")
    public EnsaioResponseDTO removerPresenca(@PathVariable Long ensaioId, @PathVariable Long musicoId) {
        return service.removerPresenca(ensaioId, musicoId);
    }

    @DeleteMapping("/{id}")
    public EnsaioResponseDTO excluir(@PathVariable long id) {
        return service.excluir(id);
    }
}
