package br.cefetmg.ocva.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.cefetmg.ocva.model.CodigoAcesso;
import br.cefetmg.ocva.dto.CodigoAcessoVerificacaoResponseDTO;
import br.cefetmg.ocva.service.CodigoAcessoService;

@RestController
@RequestMapping("/api/v1/codigos-acesso")
@CrossOrigin(origins = "*")
public class CodigoAcessoController {

    private final CodigoAcessoService service;

    public CodigoAcessoController(CodigoAcessoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CodigoAcesso> listar() {
        return service.listar();
    }

    @PostMapping("/cadastrar")
    public CodigoAcesso cadastrarCodigo(@RequestBody CadastrarCodigoRequest request) {
        return service.cadastrar(request == null ? null : request.codigo);
    }

    @DeleteMapping("/{codigo}")
    public void deletar(@PathVariable String codigo) {
        service.deletar(codigo);
    }

    @GetMapping("/verificar/{codigo}")
    public CodigoAcessoVerificacaoResponseDTO verificar(@PathVariable String codigo) {
        return service.verificar(codigo);
    }

    @PostMapping("/validar")
    public ResponseEntity<CodigoAcesso> validarEUsarCodigo(@RequestBody Map<String, Object> payload) {
        String codigo = payload == null ? null : (String) payload.get("codigo");
        Long usuarioId = payload != null && payload.get("usuarioId") != null
                ? Long.valueOf(payload.get("usuarioId").toString()) : null;
        String usuarioNome = payload != null ? (String) payload.get("usuarioNome") : null;
        return ResponseEntity.ok(service.validarEUsar(codigo, usuarioId, usuarioNome));
    }

    public static class CadastrarCodigoRequest {
        public String codigo;
    }
}
