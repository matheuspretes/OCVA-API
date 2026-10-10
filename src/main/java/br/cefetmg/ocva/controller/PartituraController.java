package br.cefetmg.ocva.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.cefetmg.ocva.model.Partitura;
import br.cefetmg.ocva.service.PartituraService;

@RestController
@RequestMapping("/api/partituras")
@CrossOrigin(origins = "*")
public class PartituraController {

    private final PartituraService service;

    public PartituraController(PartituraService service) {
        this.service = service;
    }

    @GetMapping
    public List<Partitura> listar() {
        return service.listar();
    }

    @PostMapping("/upload")
    public ResponseEntity<Partitura> uploadPartitura(
            @RequestParam("partitura") MultipartFile file,
            @RequestParam("nome") String nome,
            @RequestParam(required = false, defaultValue = "") String compositor,
            @RequestParam(required = false, defaultValue = "") String instrumento,
            @RequestParam(required = false, defaultValue = "") String categoria,
            @RequestParam(required = false, defaultValue = "") String evento) {
        Partitura partitura = service.upload(file, nome, compositor, instrumento, categoria, evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(partitura);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
