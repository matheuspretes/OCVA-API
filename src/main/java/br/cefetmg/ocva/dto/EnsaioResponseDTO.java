package br.cefetmg.ocva.dto;

import java.util.List;

import br.cefetmg.ocva.model.Ensaio;
import lombok.Getter;

@Getter
public class EnsaioResponseDTO {

    private final Long id;
    private final String data;
    private final String descricao;
    private final String titulo;
    private final List<MusicoResumoDTO> musicos;
    private final List<MusicoResumoDTO> presencas;
    private final List<MusicoResumoDTO> faltas;
    private final MusicoResumoDTO criador;

    public EnsaioResponseDTO(Ensaio ensaio) {
        this.id = ensaio.getId();
        this.data = ensaio.getData();
        this.descricao = ensaio.getDescricao();
        this.titulo = ensaio.getTitulo();
        this.musicos = converter(ensaio.getMusicos());
        this.presencas = converter(ensaio.getPresencas());
        this.faltas = converter(ensaio.getFaltas());
        this.criador = ensaio.getCriador() == null
                ? null : new MusicoResumoDTO(ensaio.getCriador());
    }

    private static List<MusicoResumoDTO> converter(List<br.cefetmg.ocva.model.Musico> musicos) {
        return musicos == null ? List.of() : musicos.stream().map(MusicoResumoDTO::new).toList();
    }
}
