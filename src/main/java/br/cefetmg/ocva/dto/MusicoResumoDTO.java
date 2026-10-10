package br.cefetmg.ocva.dto;

import br.cefetmg.ocva.model.Musico;
import lombok.Getter;

@Getter
public class MusicoResumoDTO {

    private final Long id;
    private final String nome;
    private final String instrumento;
    private final String tipo;
    private final Boolean ativo;

    public MusicoResumoDTO(Musico musico) {
        this.id = musico.getId();
        this.nome = musico.getNome();
        this.instrumento = musico.getInstrumento();
        this.tipo = musico.getTipo();
        this.ativo = musico.getAtivo();
    }
}
