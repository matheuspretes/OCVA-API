package br.cefetmg.ocva.dto;

import java.util.List;

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
    private List<String> instrumentos;
    private String codigoAcesso;
    private Boolean ativo;
}
