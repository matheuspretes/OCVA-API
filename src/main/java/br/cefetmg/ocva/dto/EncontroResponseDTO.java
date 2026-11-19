package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.Encontro;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.TipoEncontro;
import lombok.Getter;

@Getter
public class EncontroResponseDTO {

    private final Long id;
    private final String data;
    private final String hora;
    private final String titulo;
    private final String descricao;
    private final TipoEncontro tipo;
    private final MusicoResumoDTO criador;
    private final List<MusicoResumoDTO> interessados;
    private final List<MusicoResumoDTO> recusados;

    public EncontroResponseDTO(Encontro encontro) {
        this.id = encontro.getId();
        this.data = encontro.getData() == null ? null : encontro.getData().toString();
        this.hora = encontro.getHora() == null ? null : encontro.getHora().toString();
        this.titulo = encontro.getTitulo();
        this.descricao = encontro.getDescricao();
        this.tipo = encontro.getTipo();
        this.criador = encontro.getCriador() == null ? null : new MusicoResumoDTO(encontro.getCriador());
        this.interessados = converter(encontro.getInteressados());
        this.recusados = converter(encontro.getRecusados());
    }

    private static List<MusicoResumoDTO> converter(List<Musico> musicos) {
        return musicos == null ? List.of() : musicos.stream().map(MusicoResumoDTO::new).toList();
    }
}
