package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.EventoMarcado;
import br.cefetmg.ocva.model.Musico;
import lombok.Getter;

@Getter
public class EventoMarcadoResponseDTO {

    private final Long id;
    private final String data;
    private final String hora;
    private final String titulo;
    private final String local;
    private final String descricao;
    private final String banner;
    private final MusicoResumoDTO criador;
    private final MusicoResumoDTO maestro;
    private final List<MusicoResumoDTO> interessados;
    private final List<MusicoResumoDTO> recusados;

    public EventoMarcadoResponseDTO(EventoMarcado evento) {
        this.id = evento.getId();
        this.data = evento.getData() == null ? null : evento.getData().toString();
        this.hora = evento.getHora() == null ? null : evento.getHora().toString();
        this.titulo = evento.getTitulo();
        this.maestro = evento.getMaestro() == null ? null : new MusicoResumoDTO(evento.getMaestro());
        this.local = evento.getLocal();
        this.descricao = evento.getDescricao();
        this.banner = evento.getBanner();
        this.criador = evento.getCriador() == null ? null : new MusicoResumoDTO(evento.getCriador());
        this.interessados = converter(evento.getInteressados());
        this.recusados = converter(evento.getRecusados());
    }

    private static List<MusicoResumoDTO> converter(List<Musico> musicos) {
        return musicos == null ? List.of() : musicos.stream().map(MusicoResumoDTO::new).toList();
    }
}
