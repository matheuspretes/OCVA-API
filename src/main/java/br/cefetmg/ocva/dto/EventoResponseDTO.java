package br.cefetmg.ocva.dto;

import java.util.ArrayList;
import java.util.List;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.InstrumentoNecessario;
import br.cefetmg.ocva.model.Partitura;
import lombok.Getter;

@Getter
public class EventoResponseDTO {

    private final Long id;
    private final String data;
    private final String descricao;
    private final String titulo;
    private final String local;
    private final String banner;
    private final List<MusicoResumoDTO> musicos;
    private final List<InstrumentoNecessarioResponseDTO> instrumentos;
    private final List<Partitura> partituras;

    public EventoResponseDTO(Evento evento) {
        this.id = evento.getId();
        this.data = evento.getData();
        this.descricao = evento.getDescricao();
        this.titulo = evento.getTitulo();
        this.local = evento.getLocal();
        this.banner = evento.getBanner();
        this.musicos = evento.getMusicos() == null
                ? List.of()
                : evento.getMusicos().stream().map(MusicoResumoDTO::new).toList();

        this.instrumentos = new ArrayList<>();
        if (evento.getInstrumentos() != null) {
            for (InstrumentoNecessario item : evento.getInstrumentos()) {
                this.instrumentos.add(new InstrumentoNecessarioResponseDTO(item, evento.getMusicos()));
            }
        }

        this.partituras = evento.getPartituras() == null ? List.of() : evento.getPartituras();
    }
}
