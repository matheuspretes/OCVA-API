package br.cefetmg.ocva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Categoria de um encontro (ex.: "Ensaio", "Reunião").
 * Novas categorias podem ser cadastradas pelos usuários.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tipo_encontro")
public class TipoEncontro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false, unique = true)
    private String nome;

    // Encontros do tipo "Ensaio" viram ensaio quando passam e precisam de um evento
    public boolean ehEnsaio() {
        return "ensaio".equalsIgnoreCase(nome == null ? "" : nome.trim());
    }
}
