package br.cefetmg.ocva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.EnsaioRepository;

/**
 * Transforma em ensaio todo encontro cuja data já passou.
 *
 * Roda todo dia logo após a meia-noite, na inicialização da API e antes das
 * consultas de encontros. Assim a conversão acontece mesmo que o servidor
 * estivesse desligado na virada do dia.
 */
@Component
public class EncontroConversor {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_ENSAIO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(EncontroConversor.class);

    private final EncontroRepository encontroRepository;
    private final EnsaioRepository ensaioRepository;

    public EncontroConversor(EncontroRepository encontroRepository, EnsaioRepository ensaioRepository) {
        this.encontroRepository = encontroRepository;
        this.ensaioRepository = ensaioRepository;
    }

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        converterEncontrosPassados();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void diariamente() {
        converterEncontrosPassados();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized int converterEncontrosPassados() {
        List<Encontro> passados = encontroRepository.findByDataBefore(hoje());
        for (Encontro encontro : passados) {
            ensaioRepository.save(paraEnsaio(encontro));
            encontroRepository.delete(encontro);
        }
        if (!passados.isEmpty()) {
            log.info("{} encontro(s) convertido(s) em ensaio.", passados.size());
        }
        return passados.size();
    }

    private Ensaio paraEnsaio(Encontro encontro) {
        Ensaio ensaio = new Ensaio();
        ensaio.setData(LocalDateTime.of(encontro.getData(), encontro.getHora()).format(FORMATO_ENSAIO));
        ensaio.setTitulo(encontro.getTitulo());
        ensaio.setTipo(encontro.getTipo() == null ? null : encontro.getTipo().getNome());
        ensaio.setDescricao(encontro.getDescricao());
        ensaio.setCriador(encontro.getCriador());

        List<Musico> interessados = copiar(encontro.getInteressados());
        List<Musico> recusados = copiar(encontro.getRecusados());

        // Quem respondeu à enquete fica convocado; "Vou" vira presença e "Não vou" vira falta
        Map<Long, Musico> convocados = new LinkedHashMap<>();
        interessados.forEach(m -> convocados.put(m.getId(), m));
        recusados.forEach(m -> convocados.put(m.getId(), m));

        ensaio.setMusicos(new ArrayList<>(convocados.values()));
        ensaio.setPresencas(interessados);
        ensaio.setFaltas(recusados);
        ensaio.setInstrumentos(new ArrayList<>());
        ensaio.setPartituras(new ArrayList<>());
        return ensaio;
    }

    private static List<Musico> copiar(List<Musico> lista) {
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }
}
