package br.cefetmg.ocva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.dto.EventoMarcadoRequestDTO;
import br.cefetmg.ocva.dto.EventoMarcadoResponseDTO;
import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.EventoMarcado;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.EnsaioRepository;
import br.cefetmg.ocva.repository.EventoMarcadoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

@Service
public class EventoMarcadoService {

    private final EventoMarcadoRepository repository;
    private final MusicoRepository musicoRepository;
    private final EnsaioRepository ensaioRepository;
    private final EncontroRepository encontroRepository;
    private final AtualizadorAgenda atualizador;
    private final AgendaReferencias referencias;

    public EventoMarcadoService(EventoMarcadoRepository repository, MusicoRepository musicoRepository,
            EnsaioRepository ensaioRepository, EncontroRepository encontroRepository,
            AtualizadorAgenda atualizador, AgendaReferencias referencias) {
        this.repository = repository;
        this.musicoRepository = musicoRepository;
        this.ensaioRepository = ensaioRepository;
        this.encontroRepository = encontroRepository;
        this.atualizador = atualizador;
        this.referencias = referencias;
    }

    /** Eventos que ainda não aconteceram, do mais próximo para o mais distante. */
    @Transactional
    public List<EventoMarcadoResponseDTO> listar() {
        atualizador.atualizar();
        List<EventoMarcadoResponseDTO> futuros = new ArrayList<>();
        for (EventoMarcado evento : repository.findAllByOrderByDataAscHoraAsc()) {
            if (!EncontroConversor.jaPassou(evento.getData(), evento.getHora())) {
                futuros.add(new EventoMarcadoResponseDTO(evento));
            }
        }
        return futuros;
    }

    @Transactional
    public EventoMarcadoResponseDTO proximo() {
        List<EventoMarcadoResponseDTO> futuros = listar();
        return futuros.isEmpty() ? null : futuros.get(0);
    }

    @Transactional(readOnly = true)
    public EventoMarcadoResponseDTO buscarPorId(Long id) {
        return new EventoMarcadoResponseDTO(buscar(id));
    }

    @Transactional
    public EventoMarcadoResponseDTO inserir(EventoMarcadoRequestDTO dto) {
        EventoMarcado evento = new EventoMarcado();
        preencher(evento, dto, true);
        evento.setInteressados(new ArrayList<>());
        evento.setRecusados(new ArrayList<>());
        return new EventoMarcadoResponseDTO(repository.save(evento));
    }

    @Transactional
    public EventoMarcadoResponseDTO atualizar(EventoMarcadoRequestDTO dto) {
        if (dto.getId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "id é obrigatório");
        }
        EventoMarcado evento = buscar(dto.getId());
        preencher(evento, dto, false);
        return new EventoMarcadoResponseDTO(repository.save(evento));
    }

    @Transactional
    public EventoMarcadoResponseDTO excluir(Long id) {
        EventoMarcado evento = buscar(id);
        EventoMarcadoResponseDTO resposta = new EventoMarcadoResponseDTO(evento);
        evento.getInteressados().clear();
        evento.getRecusados().clear();

        // Os ensaios do evento ficam sem evento
        for (Ensaio ensaio : ensaioRepository.findByEventoMarcadoId(id)) {
            ensaio.setEventoMarcado(null);
        }
        for (Encontro encontro : encontroRepository.findByEventoMarcadoId(id)) {
            encontro.setEventoMarcado(null);
        }
        repository.delete(evento);
        return resposta;
    }

    /** "Vou" (interessado = true) ou "Não vou". Responder de novo troca a resposta. */
    @Transactional
    public EventoMarcadoResponseDTO responder(Long eventoId, Long musicoId, boolean interessado) {
        EventoMarcado evento = buscar(eventoId);
        Musico musico = buscarMusico(musicoId);
        limparResposta(evento, musicoId);
        (interessado ? evento.getInteressados() : evento.getRecusados()).add(musico);
        return new EventoMarcadoResponseDTO(repository.save(evento));
    }

    @Transactional
    public EventoMarcadoResponseDTO removerResposta(Long eventoId, Long musicoId) {
        EventoMarcado evento = buscar(eventoId);
        buscarMusico(musicoId);
        limparResposta(evento, musicoId);
        return new EventoMarcadoResponseDTO(repository.save(evento));
    }

    private void preencher(EventoMarcado evento, EventoMarcadoRequestDTO dto, boolean novo) {
        LocalDate data = lerData(dto.getData());
        LocalTime hora = lerHora(dto.getHora());

        boolean mudouData = novo || !data.equals(evento.getData()) || !hora.equals(evento.getHora());
        if (mudouData) {
            LocalDateTime agora = LocalDateTime.now(EncontroConversor.FUSO);
            LocalDateTime quando = LocalDateTime.of(data, hora);
            if (quando.isBefore(agora)) {
                throw erro(HttpStatus.BAD_REQUEST, "Não é possível marcar um evento em uma data que já passou.");
            }
            if (quando.isAfter(agora.plusMonths(AgendaValidador.MESES_MAXIMO_ANTECEDENCIA))) {
                throw erro(HttpStatus.BAD_REQUEST, "A data não pode ser mais de "
                        + AgendaValidador.MESES_MAXIMO_ANTECEDENCIA + " meses à frente.");
            }
        }

        evento.setData(data);
        evento.setHora(hora);
        evento.setTitulo(texto(dto.getTitulo(), "o título", 120));
        evento.setLocal(texto(dto.getLocal(), "o local", 160));
        evento.setDescricao(texto(dto.getDescricao(), "a descrição", 5000));
        evento.setBanner(dto.getBanner() == null || dto.getBanner().isBlank() ? null : dto.getBanner());
        evento.setMaestro(referencias.buscarMaestro(dto.getMaestroId(), evento.getMaestro()));
        if (novo || dto.getCriadorId() != null) {
            evento.setCriador(dto.getCriadorId() == null ? null : buscarMusico(dto.getCriadorId()));
        }
    }

    private void limparResposta(EventoMarcado evento, Long musicoId) {
        if (evento.getInteressados() == null) {
            evento.setInteressados(new ArrayList<>());
        }
        if (evento.getRecusados() == null) {
            evento.setRecusados(new ArrayList<>());
        }
        evento.getInteressados().removeIf(m -> musicoId.equals(m.getId()));
        evento.getRecusados().removeIf(m -> musicoId.equals(m.getId()));
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

    private EventoMarcado buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Evento marcado não encontrado"));
    }

    private Musico buscarMusico(Long id) {
        return musicoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Músico não encontrado"));
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
