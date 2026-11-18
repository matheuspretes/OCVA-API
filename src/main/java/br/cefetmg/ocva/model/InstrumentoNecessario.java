package br.cefetmg.ocva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa um pedido de músicos para um ensaio ou evento.
 * Exemplo: instrumento = "Violão", quantidade = 2.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
public class InstrumentoNecessario {

    @Column(length = 60, nullable = false)
    private String instrumento;

    @Column(nullable = false)
    private Integer quantidade;
}
