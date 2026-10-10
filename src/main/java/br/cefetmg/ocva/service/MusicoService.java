package br.cefetmg.ocva.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.CodigoAcesso;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.dto.MusicoRequestDTO;
import br.cefetmg.ocva.dto.MusicoResponseDTO;
import br.cefetmg.ocva.repository.CodigoAcessoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

@Service
public class MusicoService {

    private final MusicoRepository musicoRepository;
    private final CodigoAcessoRepository codigoAcessoRepository;

    public MusicoService(MusicoRepository musicoRepository, CodigoAcessoRepository codigoAcessoRepository) {
        this.musicoRepository = musicoRepository;
        this.codigoAcessoRepository = codigoAcessoRepository;
    }

    @Transactional(readOnly = true)
    public List<MusicoResponseDTO> listar() {
        return musicoRepository.findAll().stream().map(MusicoResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public MusicoResponseDTO buscarPorId(Long id) {
        return musicoRepository.findById(id).map(MusicoResponseDTO::new).orElse(null);
    }

    @Transactional
    public MusicoResponseDTO inserir(MusicoRequestDTO dto) {
        Musico musico = converter(dto);
        musico.setId(null);
        if (musico.getAtivo() == null) {
            musico.setAtivo(true);
        }

        Musico salvo = musicoRepository.save(musico);
        associarCodigoDeAcesso(salvo, musico.getCodigoAcesso());
        return new MusicoResponseDTO(salvo);
    }

    @Transactional
    public MusicoResponseDTO atualizar(MusicoRequestDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id é obrigatório");
        }

        Musico musico = musicoRepository.findById(dto.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Musico com id: " + dto.getId() + " não encontrado"));
        String senhaAtual = musico.getSenha();
        Musico dados = converter(dto);
        musico.setNome(dados.getNome());
        musico.setIdade(dados.getIdade());
        musico.setLogin(dados.getLogin());
        musico.setSenha(dados.getSenha() == null || dados.getSenha().isBlank() ? senhaAtual : dados.getSenha());
        musico.setTipo(dados.getTipo());
        musico.setInstrumento(dados.getInstrumento());
        musico.setAtivo(dados.getAtivo() == null ? true : dados.getAtivo());
        if (!musicoRepository.existsById(musico.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Musico com id: " + musico.getId() + " não encontrado");
        }
        return new MusicoResponseDTO(musicoRepository.save(musico));
    }

    @Transactional
    public Musico excluir(long id) {
        Musico musico = musicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Musico com id: " + id + " não encontrado"));
        musicoRepository.delete(musico);
        return musico;
    }

    @Transactional(readOnly = true)
    public MusicoResponseDTO login(String login, String senha) {
        Musico musico = musicoRepository.findByLoginAndSenha(login, senha)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Login ou senha inválidos"));
        if (Boolean.FALSE.equals(musico.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário suspenso");
        }
        return new MusicoResponseDTO(musico);
    }

    @Transactional(readOnly = true)
    public boolean loginExiste(String login) {
        return musicoRepository.findByLogin(login).isPresent();
    }

    private void associarCodigoDeAcesso(Musico musico, String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return;
        }

        CodigoAcesso codigoAcesso = codigoAcessoRepository
                .findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Código de acesso não encontrado."));

        if (codigoAcesso.FoiUtilizado()
                || (codigoAcesso.getDataExpiracao() != null
                && codigoAcesso.getDataExpiracao().isBefore(LocalDateTime.now()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Código de acesso já utilizado ou expirado.");
        }

        codigoAcesso.setStatus("utilizado");
        codigoAcesso.setDataUso(LocalDateTime.now());
        codigoAcesso.setUsuarioId(musico.getId());
        codigoAcesso.setUsuarioNome(musico.getNome());
        codigoAcessoRepository.save(codigoAcesso);
    }

    private Musico converter(MusicoRequestDTO dto) {
        Musico musico = new Musico();
        musico.setIdade(dto.getIdade());
        musico.setNome(dto.getNome());
        musico.setLogin(dto.getLogin());
        musico.setSenha(dto.getSenha());
        musico.setTipo(dto.getTipo());
        musico.setInstrumento(dto.getInstrumento());
        musico.setCodigoAcesso(dto.getCodigoAcesso());
        musico.setAtivo(dto.getAtivo());
        return musico;
    }
}
