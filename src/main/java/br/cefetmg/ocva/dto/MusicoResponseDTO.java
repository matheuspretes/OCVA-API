package br.cefetmg.ocva.dto;

import br.cefetmg.ocva.model.Musico;
import lombok.Getter;

@Getter
public class MusicoResponseDTO {

    private final Long id;
    private final String nome;
    private final int idade;
    private final String login;
    private final String tipo;
    private final String instrumento;
    private final Boolean ativo;

    public MusicoResponseDTO(Musico musico) {
        this.id = musico.getId();
        this.nome = musico.getNome();
        this.idade = musico.getIdade();
        this.login = musico.getLogin();
        this.tipo = musico.getTipo();
        this.instrumento = musico.getInstrumento();
        this.ativo = musico.getAtivo();
    }
}
