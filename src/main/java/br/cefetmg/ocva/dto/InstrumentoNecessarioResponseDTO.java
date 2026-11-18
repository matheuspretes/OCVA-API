package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.InstrumentoNecessario;
import br.cefetmg.ocva.model.Musico;
import lombok.Getter;

@Getter
public class InstrumentoNecessarioResponseDTO {

    private final String instrumento;
    private final Integer quantidade;
    private final int escalados;

    public InstrumentoNecessarioResponseDTO(InstrumentoNecessario item, List<Musico> musicos) {
        this.instrumento = item.getInstrumento();
        this.quantidade = item.getQuantidade();
        this.escalados = contarEscalados(item.getInstrumento(), musicos);
    }

    private static int contarEscalados(String instrumento, List<Musico> musicos) {
        int total = 0;
        if (musicos == null) {
            return total;
        }
        for (Musico musico : musicos) {
            if (instrumento.equalsIgnoreCase(musico.getInstrumento())) {
                total++;
            }
        }
        return total;
    }
}
