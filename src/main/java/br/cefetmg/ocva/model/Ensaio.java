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
@Table(name = "ensaio")
public class Ensaio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String data;

    @Column(length = 255, nullable = false)
    private String descricao;

    @Column(length = 120)
    private String titulo;

    // Tipo do encontro que originou o ensaio (ex.: "Reunião"); vazio = ensaio comum
    @Column(length = 40)
    private String tipo;

    @ManyToMany
    @JoinTable(
        name = "ensaio_musico",
        joinColumns = @JoinColumn(name = "ensaio_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> musicos;

    @ManyToMany
    @JoinTable(
        name = "ensaio_presenca",
        joinColumns = @JoinColumn(name = "ensaio_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> presencas;

    @ManyToMany
    @JoinTable(
        name = "ensaio_falta",
        joinColumns = @JoinColumn(name = "ensaio_id"),
        inverseJoinColumns = @JoinColumn(name = "musico_id")
    )
    private List<Musico> faltas;

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

    @ElementCollection
    @CollectionTable(
        name = "ensaio_instrumento",
        joinColumns = @JoinColumn(name = "ensaio_id")
    )
    private List<InstrumentoNecessario> instrumentos;

    @ManyToMany
    @JoinTable(
        name = "ensaio_partitura",
        joinColumns = @JoinColumn(name = "ensaio_id"),
        inverseJoinColumns = @JoinColumn(name = "partitura_id")
    )
    private List<Partitura> partituras;
}
