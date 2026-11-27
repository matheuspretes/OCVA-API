package br.cefetmg.ocva.dto;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.EventoMarcado;
import lombok.Getter;

/**
 * Dados básicos do evento ligado a um ensaio.
 * "marcado" = true quando o evento ainda não aconteceu (EventoMarcado).
 */
@Getter
public class EventoResumoDTO {

    private final Long id;
    private final String titulo;
    private final String data;
    private final boolean marcado;

    public EventoResumoDTO(EventoMarcado evento) {
        this.id = evento.getId();
        this.titulo = evento.getTitulo();
        this.data = evento.getData() + "T" + evento.getHora();
        this.marcado = true;
    }

    public EventoResumoDTO(Evento evento) {
        this.id = evento.getId();
        this.titulo = evento.getTitulo();
        this.data = evento.getData();
        this.marcado = false;
    }

    // Devolve o evento que estiver preenchido (ou null se nenhum)
    public static EventoResumoDTO de(EventoMarcado marcado, Evento evento) {
        if (marcado != null) {
            return new EventoResumoDTO(marcado);
        }
        if (evento != null) {
            return new EventoResumoDTO(evento);
        }
        return null;
    }
}
