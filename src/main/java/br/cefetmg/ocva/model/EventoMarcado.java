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
 * Um evento marcado que ainda não aconteceu.
 * Depois que o dia passa, ele é convertido em um {@link Evento} e quem
 * respondeu "Vou" fica registrado como participante.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "evento_marcado")
public class EventoMarcado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(length = 120, nullable = false)
    private String titulo;

    @Column(length = 160, nullable = false)
    private String local;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String banner;

    @ManyToOne
    @JoinColumn(name = "criador_id")
    private Musico criador;

    // Maestro responsável
    @ManyToOne
    @JoinColumn(name = "maestro_id")
    private Musico maestro;

    // Músicos que responderam "Vou"
    @ManyToMany
    @JoinTable(
        name = "evento_marcado_interessado",
        joinColumns = @JoinColumn(name = "evento_marcado_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> interessados;

    // Músicos que responderam "Não vou"
    @ManyToMany
    @JoinTable(
        name = "evento_marcado_recusado",
        joinColumns = @JoinColumn(name = "evento_marcado_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> recusados;
}
