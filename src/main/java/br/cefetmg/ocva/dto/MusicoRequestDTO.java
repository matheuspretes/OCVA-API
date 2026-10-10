package br.cefetmg.ocva.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicoRequestDTO {

    private Long id;
    private String nome;
    private int idade;
    private String login;
    private String senha;
    private String tipo;
    private String instrumento;
    private String codigoAcesso;
    private Boolean ativo;
}
