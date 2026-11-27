package br.cefetmg.ocva.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "evento")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String data;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(length = 120)
    private String titulo;

    @Column(length = 160)
    private String local;

    @Column(columnDefinition = "TEXT")
    private String banner;

    // Maestro responsável
    @ManyToOne
    @JoinColumn(name = "maestro_id")
    private Musico maestro;

    @ManyToMany
    @JoinTable(
        name = "evento_musico",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> musicos;

    // Presença registrada depois do evento (como nos ensaios)
    @ManyToMany
    @JoinTable(
        name = "evento_presenca",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> presencas;

    @ManyToMany
    @JoinTable(
        name = "evento_falta",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> faltas;

    @ElementCollection
    @CollectionTable(
        name = "evento_instrumento",
        joinColumns = @JoinColumn(name = "evento_id")
    )
    private List<InstrumentoNecessario> instrumentos;

    @ManyToMany
    @JoinTable(
        name = "evento_partitura",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "partitura_id")
    )
    private List<Partitura> partituras;
}
