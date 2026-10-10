package br.cefetmg.ocva.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnsaioRequestDTO {

    private Long id;
    private String data;
    private String descricao;
    private String titulo;
    private List<Long> musicoIds;
    private Long criadorId;
}
