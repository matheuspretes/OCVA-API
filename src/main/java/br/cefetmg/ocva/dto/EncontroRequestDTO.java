package br.cefetmg.ocva.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncontroRequestDTO {

    private Long id;
    // aaaa-mm-dd
    private String data;
    // hh:mm
    private String hora;
    private String titulo;
    private String descricao;
    private Long tipoId;
    private Long criadorId;
}
