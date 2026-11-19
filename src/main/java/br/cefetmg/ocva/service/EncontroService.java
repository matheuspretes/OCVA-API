package br.cefetmg.ocva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.dto.EncontroRequestDTO;
import br.cefetmg.ocva.dto.EncontroResponseDTO;
import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.TipoEncontro;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.MusicoRepository;
import br.cefetmg.ocva.repository.TipoEncontroRepository;

@Service
public class EncontroService {

    public static final List<String> TIPOS_INICIAIS = List.of("Ensaio", "Reunião");

    private final EncontroRepository encontroRepository;
    private final TipoEncontroRepository tipoRepository;
    private final MusicoRepository musicoRepository;
    private final EncontroConversor conversor;

    public EncontroService(EncontroRepository encontroRepository, TipoEncontroRepository tipoRepository,
            MusicoRepository musicoRepository, EncontroConversor conversor) {
        this.encontroRepository = encontroRepository;
        this.tipoRepository = tipoRepository;
        this.musicoRepository = musicoRepository;
        this.conversor = conversor;
    }

    // ---------- Tipos ----------

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    @Transactional
    public void criarTiposIniciais() {
        for (String nome : TIPOS_INICIAIS) {
            if (!tipoRepository.existsByNomeIgnoreCase(nome)) {
                tipoRepository.save(new TipoEncontro(null, nome));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TipoEncontro> listarTipos() {
        return tipoRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public TipoEncontro inserirTipo(String nome) {
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o nome da categoria.");
        }
        if (limpo.length() > 40) {
            throw erro(HttpStatus.BAD_REQUEST, "O nome da categoria pode ter no máximo 40 caracteres.");
        }
        if (tipoRepository.existsByNomeIgnoreCase(limpo)) {
            throw erro(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        }
        return tipoRepository.save(new TipoEncontro(null, limpo));
    }

    // ---------- Encontros ----------

    @Transactional
    public List<EncontroResponseDTO> listar() {
        conversor.converterEncontrosPassados();
        return encontroRepository.findByDataGreaterThanEqualOrderByDataAscHoraAsc(EncontroConversor.hoje())
                .stream().map(EncontroResponseDTO::new).toList();
    }

    @Transactional
    public EncontroResponseDTO proximo() {
        conversor.converterEncontrosPassados();
        return encontroRepository.findByDataGreaterThanEqualOrderByDataAscHoraAsc(EncontroConversor.hoje())
                .stream().findFirst().map(EncontroResponseDTO::new).orElse(null);
    }

    @Transactional(readOnly = true)
    public EncontroResponseDTO buscarPorId(Long id) {
        return new EncontroResponseDTO(buscarEncontro(id));
    }

    @Transactional
    public EncontroResponseDTO inserir(EncontroRequestDTO dto) {
        Encontro encontro = new Encontro();
        preencher(encontro, dto, true);
        encontro.setInteressados(new ArrayList<>());
        encontro.setRecusados(new ArrayList<>());
        return new EncontroResponseDTO(encontroRepository.save(encontro));
    }

    @Transactional
    public EncontroResponseDTO atualizar(EncontroRequestDTO dto) {
        if (dto.getId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "id é obrigatório");
        }
        Encontro encontro = buscarEncontro(dto.getId());
        preencher(encontro, dto, false);
        return new EncontroResponseDTO(encontroRepository.save(encontro));
    }

    @Transactional
    public EncontroResponseDTO excluir(Long id) {
        Encontro encontro = buscarEncontro(id);
        EncontroResponseDTO resposta = new EncontroResponseDTO(encontro);
        encontro.getInteressados().clear();
        encontro.getRecusados().clear();
        encontroRepository.delete(encontro);
        return resposta;
    }

    /**
     * Registra a resposta do músico na enquete: "Vou" (interessado = true) ou "Não vou".
     * Responder de novo troca a resposta anterior.
     */
    @Transactional
    public EncontroResponseDTO responder(Long encontroId, Long musicoId, boolean interessado) {
        Encontro encontro = buscarEncontro(encontroId);
        Musico musico = buscarMusico(musicoId);
        removerResposta(encontro, musicoId);
        (interessado ? encontro.getInteressados() : encontro.getRecusados()).add(musico);
        return new EncontroResponseDTO(encontroRepository.save(encontro));
    }

    @Transactional
    public EncontroResponseDTO removerResposta(Long encontroId, Long musicoId) {
        Encontro encontro = buscarEncontro(encontroId);
        buscarMusico(musicoId);
        removerResposta(encontro, musicoId);
        return new EncontroResponseDTO(encontroRepository.save(encontro));
    }

    // ---------- Auxiliares ----------

    private void preencher(Encontro encontro, EncontroRequestDTO dto, boolean novo) {
        LocalDate data = lerData(dto.getData());
        LocalTime hora = lerHora(dto.getHora());

        // Na edição só valida a data se ela mudou
        boolean mudouData = novo || !data.equals(encontro.getData()) || !hora.equals(encontro.getHora());
        if (mudouData) {
            LocalDateTime agora = LocalDateTime.now(EncontroConversor.FUSO);
            LocalDateTime quando = LocalDateTime.of(data, hora);
            if (quando.isBefore(agora)) {
                throw erro(HttpStatus.BAD_REQUEST, "Não é possível marcar um encontro em uma data que já passou.");
            }
            if (quando.isAfter(agora.plusMonths(AgendaValidador.MESES_MAXIMO_ANTECEDENCIA))) {
                throw erro(HttpStatus.BAD_REQUEST, "A data não pode ser mais de "
                        + AgendaValidador.MESES_MAXIMO_ANTECEDENCIA + " meses à frente.");
            }
        }

        String titulo = texto(dto.getTitulo(), "o título", 120);
        String descricao = texto(dto.getDescricao(), "a descrição", 255);

        if (dto.getTipoId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione o tipo do encontro.");
        }
        TipoEncontro tipo = tipoRepository.findById(dto.getTipoId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Tipo de encontro não encontrado."));

        encontro.setData(data);
        encontro.setHora(hora);
        encontro.setTitulo(titulo);
        encontro.setDescricao(descricao);
        encontro.setTipo(tipo);
        if (novo || dto.getCriadorId() != null) {
            encontro.setCriador(dto.getCriadorId() == null ? null : buscarMusico(dto.getCriadorId()));
        }
    }

    private void removerResposta(Encontro encontro, Long musicoId) {
        if (encontro.getInteressados() == null) {
            encontro.setInteressados(new ArrayList<>());
        }
        if (encontro.getRecusados() == null) {
            encontro.setRecusados(new ArrayList<>());
        }
        encontro.getInteressados().removeIf(m -> musicoId.equals(m.getId()));
        encontro.getRecusados().removeIf(m -> musicoId.equals(m.getId()));
    }

    private static LocalDate lerData(String valor) {
        if (valor == null || valor.isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "A data é obrigatória.");
        }
        try {
            return LocalDate.parse(valor.length() > 10 ? valor.substring(0, 10) : valor);
        } catch (DateTimeParseException e) {
            throw erro(HttpStatus.BAD_REQUEST, "Data inválida. Use o formato aaaa-mm-dd.");
        }
    }

    private static LocalTime lerHora(String valor) {
        if (valor == null || valor.isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "A hora é obrigatória.");
        }
        try {
            return LocalTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw erro(HttpStatus.BAD_REQUEST, "Hora inválida. Use o formato hh:mm.");
        }
    }

    private static String texto(String valor, String campo, int limite) {
        String limpo = valor == null ? "" : valor.trim();
        if (limpo.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe " + campo + ".");
        }
        if (limpo.length() > limite) {
            throw erro(HttpStatus.BAD_REQUEST, "O campo " + campo.substring(2) + " pode ter no máximo "
                    + limite + " caracteres.");
        }
        return limpo;
    }

    private Encontro buscarEncontro(Long id) {
        return encontroRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Encontro não encontrado"));
    }

    private Musico buscarMusico(Long id) {
        return musicoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Músico não encontrado"));
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
