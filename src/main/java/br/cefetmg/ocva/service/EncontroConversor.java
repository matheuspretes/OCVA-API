package br.cefetmg.ocva.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EncontroRepository;
import br.cefetmg.ocva.repository.EnsaioRepository;

/**
 * Transforma em ensaio todo encontro do tipo "Ensaio" cujo horário já passou.
 * Encontros de outros tipos (ex.: Reunião) continuam salvos como encontro.
 * Só quem respondeu à enquete entra no ensaio: "Vou" com presença e "Não vou" com falta.
 *
 * Quem chama este conversor é o {@link AtualizadorAgenda}.
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

    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }

    // true quando a data e a hora informadas já passaram
    public static boolean jaPassou(LocalDate data, LocalTime hora) {
        return LocalDateTime.of(data, hora).isBefore(agora());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized int converterEncontrosPassados() {
        int convertidos = 0;
        for (Encontro encontro : encontroRepository.findByDataLessThanEqual(hoje())) {
            boolean ehEnsaio = encontro.getTipo() != null && encontro.getTipo().ehEnsaio();
            if (ehEnsaio && jaPassou(encontro.getData(), encontro.getHora())) {
                ensaioRepository.save(paraEnsaio(encontro));
                encontroRepository.delete(encontro);
                convertidos++;
            }
        }
        if (convertidos > 0) {
            log.info("{} encontro(s) convertido(s) em ensaio.", convertidos);
        }
        return convertidos;
    }

    private Ensaio paraEnsaio(Encontro encontro) {
        Ensaio ensaio = new Ensaio();
        ensaio.setData(LocalDateTime.of(encontro.getData(), encontro.getHora()).format(FORMATO_ENSAIO));
        ensaio.setTitulo(encontro.getTitulo());
        ensaio.setTipo(encontro.getTipo().getNome());
        ensaio.setDescricao(encontro.getDescricao());
        ensaio.setCriador(encontro.getCriador());
        ensaio.setMaestro(encontro.getMaestro());
        ensaio.setEventoMarcado(encontro.getEventoMarcado());
        ensaio.setEvento(encontro.getEvento());

        // Só quem respondeu à enquete entra: "Vou" com presença e "Não vou" com falta
        List<Musico> recusados = copiar(encontro.getRecusados());
        List<Musico> escalados = PresencaPadrao.escalados(encontro.getInteressados(), recusados);

        ensaio.setMusicos(escalados);
        ensaio.setPresencas(PresencaPadrao.presentes(escalados, recusados));
        ensaio.setFaltas(recusados);
        ensaio.setInstrumentos(new ArrayList<>());
        ensaio.setPartituras(new ArrayList<>());
        return ensaio;
    }

    private static List<Musico> copiar(List<Musico> lista) {
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }
}
