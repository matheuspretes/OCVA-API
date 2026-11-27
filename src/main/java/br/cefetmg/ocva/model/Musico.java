package br.cefetmg.ocva.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "musico")
public class Musico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=60, nullable = false, unique=false)
    private String nome;
    private int Idade;

    @Column(length=60, nullable = false, unique=true)
    private String login;

    @Column(length=60, nullable = false)
    private String senha;

    @Column(length=20)
    private String tipo;

    @Column(length=60, nullable = false)
    private String instrumento;

    @Column(length = 8)
    private String codigoAcesso;

    @Column
    private Boolean ativo = true;

    // Todos os instrumentos que o músico toca. O campo "instrumento" guarda o principal (o primeiro).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "musico_instrumento", joinColumns = @JoinColumn(name = "musico_id"))
    @OrderColumn(name = "posicao")
    @Column(name = "instrumento", length = 60)
    private List<String> instrumentos = new ArrayList<>();

    /**
     * Lista de instrumentos do músico.
     * Músicos cadastrados antes da lista existir só têm o campo "instrumento".
     */
    public List<String> listaDeInstrumentos() {
        if (instrumentos != null && !instrumentos.isEmpty()) {
            return instrumentos;
        }
        List<String> lista = new ArrayList<>();
        if (instrumento != null && !instrumento.isBlank()) {
            lista.add(instrumento);
        }
        return lista;
    }

    public boolean tocaInstrumento(String nome) {
        for (String item : listaDeInstrumentos()) {
            if (item.equalsIgnoreCase(nome)) {
                return true;
            }
        }
        return false;
    }

    public boolean estaAtivo() {
        return !Boolean.FALSE.equals(ativo);
    }
}
