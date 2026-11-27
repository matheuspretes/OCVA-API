package br.cefetmg.ocva.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Registra os encontros e eventos cujo horário já passou.
 *
 * Roda a cada minuto, quando a API liga e antes de cada consulta de
 * encontros, ensaios e eventos. Assim nada fica para trás mesmo que o
 * servidor tenha ficado desligado (o Render desliga a API quando ninguém usa).
 */
@Component
public class AtualizadorAgenda {

    private final EventoMarcadoConversor eventoConversor;
    private final EncontroConversor encontroConversor;

    public AtualizadorAgenda(EventoMarcadoConversor eventoConversor, EncontroConversor encontroConversor) {
        this.eventoConversor = eventoConversor;
        this.encontroConversor = encontroConversor;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void aoIniciar() {
        atualizar();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void aCadaMinuto() {
        atualizar();
    }

    public void atualizar() {
        // Primeiro os eventos, para que os ensaios convertidos já apontem para o evento realizado
        eventoConversor.converterEventosPassados();
        encontroConversor.converterEncontrosPassados();
    }
}
