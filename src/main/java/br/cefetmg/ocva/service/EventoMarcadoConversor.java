package br.cefetmg.ocva.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.EventoMarcado;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EventoMarcadoRepository;
import br.cefetmg.ocva.repository.EventoRepository;

/**
 * Transforma em {@link Evento} todo evento marcado cuja data já passou.
 * Quem respondeu "Vou" vira participante do evento e fica com presença,
 * que pode ser trocada por falta depois.
 *
 * Roda na virada do dia, na inicialização da API e antes das consultas.
 */
@Component
public class EventoMarcadoConversor {

    private static final DateTimeFormatter FORMATO_EVENTO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(EventoMarcadoConversor.class);

    private final EventoMarcadoRepository eventoMarcadoRepository;
    private final EventoRepository eventoRepository;

    public EventoMarcadoConversor(EventoMarcadoRepository eventoMarcadoRepository, EventoRepository eventoRepository) {
        this.eventoMarcadoRepository = eventoMarcadoRepository;
        this.eventoRepository = eventoRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        converterEventosPassados();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void diariamente() {
        converterEventosPassados();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized int converterEventosPassados() {
        List<EventoMarcado> passados = eventoMarcadoRepository.findByDataBefore(EncontroConversor.hoje());
        for (EventoMarcado marcado : passados) {
            eventoRepository.save(paraEvento(marcado));
            eventoMarcadoRepository.delete(marcado);
        }
        if (!passados.isEmpty()) {
            log.info("{} evento(s) marcado(s) convertido(s) em evento.", passados.size());
        }
        return passados.size();
    }

    private Evento paraEvento(EventoMarcado marcado) {
        Evento evento = new Evento();
        evento.setData(LocalDateTime.of(marcado.getData(), marcado.getHora()).format(FORMATO_EVENTO));
        evento.setTitulo(marcado.getTitulo());
        evento.setLocal(marcado.getLocal());
        evento.setDescricao(marcado.getDescricao());
        evento.setBanner(marcado.getBanner());

        List<Musico> participantes = marcado.getInteressados() == null
                ? new ArrayList<>() : new ArrayList<>(marcado.getInteressados());
        evento.setMusicos(participantes);
        evento.setPresencas(new ArrayList<>(participantes));
        evento.setFaltas(new ArrayList<>());
        evento.setInstrumentos(new ArrayList<>());
        evento.setPartituras(new ArrayList<>());
        return evento;
    }
}
