package br.cefetmg.ocva.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import br.cefetmg.ocva.model.Partitura;
import br.cefetmg.ocva.repository.PartituraRepository;

@Service
public class PartituraService {

    private final Cloudinary cloudinary;
    private final PartituraRepository repository;

    public PartituraService(Cloudinary cloudinary, PartituraRepository repository) {
        this.cloudinary = cloudinary;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Partitura> listar() {
        return repository.findAll();
    }

    @Transactional
    public Partitura upload(MultipartFile file, String nome, String compositor,
            String instrumento, String categoria, String evento) {
        if (file.isEmpty() || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo PDF válido.");
        }
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome da partitura.");
        }

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "orquestra/partituras",
                    "resource_type", "raw",
                    "use_filename", true,
                    "unique_filename", true));

            Partitura partitura = new Partitura(
                    null, nome.trim(), (String) resultado.get("secure_url"),
                    (String) resultado.get("public_id"), compositor.trim(),
                    instrumento.trim(), categoria.trim(), evento.trim(),
                    file.getOriginalFilename(), LocalDate.now().toString());
            return repository.save(partitura);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao enviar partitura para o Cloudinary.", exception);
        }
    }

    @Transactional
    public void excluir(Long id) {
        Partitura partitura = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Partitura não encontrada."));
        try {
            cloudinary.uploader().destroy(partitura.getPublicId(),
                    ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Erro ao remover a partitura do Cloudinary.", exception);
        }
        repository.delete(partitura);
    }
}
