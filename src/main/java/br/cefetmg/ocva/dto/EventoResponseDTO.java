package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.Evento;
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
    }
}
