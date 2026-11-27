package br.cefetmg.ocva.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Um encontro marcado que ainda não aconteceu.
 * Depois que o dia do encontro passa, ele é convertido em um {@link Ensaio}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "encontro")
public class Encontro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(length = 120, nullable = false)
    private String titulo;

    @Column(length = 255, nullable = false)
    private String descricao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_id")
    private TipoEncontro tipo;

    @ManyToOne
    @JoinColumn(name = "criador_id")
    private Musico criador;

    // Maestro responsável
    @ManyToOne
    @JoinColumn(name = "maestro_id")
    private Musico maestro;

    // Evento para o qual o ensaio está sendo preparado.
    // Fica em "eventoMarcado" enquanto o evento não acontece e passa para "evento" depois.
    @ManyToOne
    @JoinColumn(name = "evento_marcado_id")
    private EventoMarcado eventoMarcado;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    // Músicos que responderam "Vou"
    @ManyToMany
    @JoinTable(
        name = "encontro_interessado",
        joinColumns = @JoinColumn(name = "encontro_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> interessados;

    // Músicos que responderam "Não vou"
    @ManyToMany
    @JoinTable(
        name = "encontro_recusado",
        joinColumns = @JoinColumn(name = "encontro_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> recusados;
}
