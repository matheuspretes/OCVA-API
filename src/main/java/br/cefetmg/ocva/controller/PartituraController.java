package br.cefetmg.ocva.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/partituras")
public class PartituraController {

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPartitura(@RequestParam("partitura") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum arquivo enviado.");
        }

        try {
            // Upload para o Cloudinary
            // "resource_type", "auto" garante que funcione tanto para PDF quanto para imagem
            Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "orquestra/partituras",
                    "resource_type", "auto"
                )
            );

            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("url", url);
            response.put("public_id", publicId);
            response.put("mensagem", "Partitura enviada com sucesso!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar partitura para o Cloudinary: " + e.getMessage());
        }
    }
}