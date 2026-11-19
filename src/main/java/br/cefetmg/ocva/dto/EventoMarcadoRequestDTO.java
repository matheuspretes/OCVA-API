package br.cefetmg.ocva.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventoMarcadoRequestDTO {

    private Long id;
    // aaaa-mm-dd
    private String data;
    // hh:mm
    private String hora;
    private String titulo;
    private String local;
    private String descricao;
    private String banner;
    private Long criadorId;
}
