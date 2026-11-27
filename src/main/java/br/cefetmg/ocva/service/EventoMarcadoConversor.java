package br.cefetmg.ocva.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.EventoMarcado;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.EnsaioRepository;
import br.cefetmg.ocva.repository.EventoMarcadoRepository;
import br.cefetmg.ocva.repository.EventoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

/**
 * Transforma em {@link Evento} todo evento marcado cujo horário já passou.
 * Todos os músicos ativos ficam escalados com presença; quem respondeu "Não vou"
 * começa com falta. A marcação pode ser trocada depois.
 * Os ensaios e encontros ligados ao evento marcado passam a apontar para o evento novo.
 *
 * Quem chama este conversor é o {@link AtualizadorAgenda}.
 */
@Component
public class EventoMarcadoConversor {

    private static final DateTimeFormatter FORMATO_EVENTO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(EventoMarcadoConversor.class);

    private final EventoMarcadoRepository eventoMarcadoRepository;
    private final EventoRepository eventoRepository;
    private final EnsaioRepository ensaioRepository;
    private final EncontroRepository encontroRepository;
    private final MusicoRepository musicoRepository;

    public EventoMarcadoConversor(EventoMarcadoRepository eventoMarcadoRepository, EventoRepository eventoRepository,
            EnsaioRepository ensaioRepository, EncontroRepository encontroRepository, MusicoRepository musicoRepository) {
        this.eventoMarcadoRepository = eventoMarcadoRepository;
        this.eventoRepository = eventoRepository;
        this.ensaioRepository = ensaioRepository;
        this.encontroRepository = encontroRepository;
        this.musicoRepository = musicoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized int converterEventosPassados() {
        int convertidos = 0;
        for (EventoMarcado marcado : eventoMarcadoRepository.findByDataLessThanEqual(EncontroConversor.hoje())) {
            if (EncontroConversor.jaPassou(marcado.getData(), marcado.getHora())) {
                Evento evento = eventoRepository.save(paraEvento(marcado));
                moverEnsaios(marcado, evento);
                eventoMarcadoRepository.delete(marcado);
                convertidos++;
            }
        }
        if (convertidos > 0) {
            log.info("{} evento(s) marcado(s) convertido(s) em evento.", convertidos);
        }
        return convertidos;
    }

    // Ensaios e encontros do evento marcado passam a apontar para o evento realizado
    private void moverEnsaios(EventoMarcado marcado, Evento evento) {
        for (Ensaio ensaio : ensaioRepository.findByEventoMarcadoId(marcado.getId())) {
            ensaio.setEventoMarcado(null);
            ensaio.setEvento(evento);
        }
        for (Encontro encontro : encontroRepository.findByEventoMarcadoId(marcado.getId())) {
            encontro.setEventoMarcado(null);
            encontro.setEvento(evento);
        }
    }

    // Músicos ativos: todos começam com presença nos ensaios e eventos registrados
    private List<Musico> musicosAtivos() {
        List<Musico> ativos = new ArrayList<>();
        for (Musico musico : musicoRepository.findByTipoIgnoreCase("musico")) {
            if (musico.estaAtivo()) {
                ativos.add(musico);
            }
        }
        return ativos;
    }

    private Evento paraEvento(EventoMarcado marcado) {
        Evento evento = new Evento();
        evento.setData(LocalDateTime.of(marcado.getData(), marcado.getHora()).format(FORMATO_EVENTO));
        evento.setTitulo(marcado.getTitulo());
        evento.setLocal(marcado.getLocal());
        evento.setDescricao(marcado.getDescricao());
        evento.setBanner(marcado.getBanner());
        evento.setMaestro(marcado.getMaestro());

        // Todos os músicos ativos ficam escalados e começam com presença;
        // quem respondeu "Não vou" começa com falta
        List<Musico> recusados = marcado.getRecusados() == null
                ? new ArrayList<>() : new ArrayList<>(marcado.getRecusados());
        List<Musico> escalados = PresencaPadrao.escalados(musicosAtivos(), marcado.getInteressados(), recusados);
        evento.setMusicos(escalados);
        evento.setPresencas(PresencaPadrao.presentes(escalados, recusados));
        evento.setFaltas(recusados);
        evento.setInstrumentos(new ArrayList<>());
        evento.setPartituras(new ArrayList<>());
        return evento;
    }
}
