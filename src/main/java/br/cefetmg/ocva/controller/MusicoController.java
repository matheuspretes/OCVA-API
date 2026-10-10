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

import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.dto.MusicoRequestDTO;
import br.cefetmg.ocva.dto.MusicoResponseDTO;
import br.cefetmg.ocva.service.MusicoService;

@RestController
@RequestMapping("/api/v1/musicos")
@CrossOrigin(origins = "*")
public class MusicoController {

    private final MusicoService service;

    public MusicoController(MusicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MusicoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MusicoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public MusicoResponseDTO inserir(@RequestBody MusicoRequestDTO dto) {
        return service.inserir(dto);
    }

    @PutMapping
    public MusicoResponseDTO atualizar(@RequestBody MusicoRequestDTO dto) {
        return service.atualizar(dto);
    }

    @DeleteMapping("/{id}")
    public Musico excluir(@PathVariable long id) {
        return service.excluir(id);
    }

    @PostMapping("/login")
    public MusicoResponseDTO login(@RequestParam String login, @RequestParam String senha) {
        return service.login(login, senha);
    }

    @GetMapping("/verificar/{login}")
    public boolean verificarLogin(@PathVariable String login) {
        return service.loginExiste(login);
    }
}
