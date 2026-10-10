package br.cefetmg.ocva.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.CodigoAcesso;
import br.cefetmg.ocva.dto.CodigoAcessoVerificacaoResponseDTO;
import br.cefetmg.ocva.repository.CodigoAcessoRepository;

@Service
public class CodigoAcessoService {

    private final CodigoAcessoRepository repository;

    public CodigoAcessoService(CodigoAcessoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CodigoAcesso> listar() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao"));
    }

    @Transactional
    public CodigoAcesso cadastrar(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código não pode ser vazio.");
        }
        String formatado = formatar(codigo);
        if (repository.existsByCodigo(formatado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este código já existe cadastrado.");
        }
        CodigoAcesso novo = new CodigoAcesso();
        novo.setCodigo(formatado);
        novo.setStatus("disponivel");
        novo.setDataCriacao(LocalDateTime.now());
        novo.setDataExpiracao(LocalDateTime.now().plusDays(30));
        return repository.save(novo);
    }

    @Transactional
    public void deletar(String codigo) {
        CodigoAcesso existente = repository.findByCodigo(formatar(codigo))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código não encontrado."));
        repository.delete(existente);
    }

    @Transactional(readOnly = true)
    public CodigoAcessoVerificacaoResponseDTO verificar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return resposta(false, "Código não informado.");
        }
        Optional<CodigoAcesso> opt = repository.findByCodigo(formatar(codigo));
        if (opt.isEmpty()) {
            return resposta(false, "Código não encontrado.");
        }
        CodigoAcesso item = opt.get();
        if (item.FoiUtilizado()) {
            return resposta(false, "Este código já foi utilizado.");
        }
        if (expirado(item)) {
            return resposta(false, "Este código está expirado.");
        }
        return resposta(true, "Código válido e disponível!");
    }

    @Transactional
    public CodigoAcesso validarEUsar(String codigo, Long usuarioId, String usuarioNome) {
        if (codigo == null || codigo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O código de acesso é obrigatório.");
        }
        CodigoAcesso item = repository.findByCodigo(formatar(codigo))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código não encontrado."));
        if (item.FoiUtilizado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este código já foi utilizado.");
        }
        if (expirado(item)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este código expirou.");
        }
        item.setUsuarioId(usuarioId);
        item.setUsuarioNome(usuarioNome);
        item.setStatus("utilizado");
        item.setDataUso(LocalDateTime.now());
        return repository.save(item);
    }

    private boolean expirado(CodigoAcesso item) {
        return item.getDataExpiracao() != null
                && item.getDataExpiracao().isBefore(LocalDateTime.now());
    }

    private String formatar(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private CodigoAcessoVerificacaoResponseDTO resposta(boolean disponivel, String mensagem) {
        return new CodigoAcessoVerificacaoResponseDTO(disponivel, mensagem);
    }
}
