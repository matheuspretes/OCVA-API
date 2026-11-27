package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.InstrumentoNecessario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnsaioRequestDTO {

    private Long id;
    private Long maestroId;
    // Evento do ensaio: preencha só um dos dois
    private Long eventoMarcadoId;
    private Long eventoId;
    private String data;
    private String descricao;
    private String titulo;
    private List<Long> musicoIds;
    private List<InstrumentoNecessario> instrumentos;
    private List<Long> partituraIds;
    private Long criadorId;
}
