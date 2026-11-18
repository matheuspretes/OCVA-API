package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.InstrumentoNecessario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventoRequestDTO {

    private Long id;
    private String data;
    private String descricao;
    private String titulo;
    private String local;
    private String banner;
    private List<Long> musicoIds;
    private List<InstrumentoNecessario> instrumentos;
    private List<Long> partituraIds;
}
