package br.cefetmg.ocva.dto;

import java.util.ArrayList;
import java.util.List;

import br.cefetmg.ocva.model.Ensaio;
import br.cefetmg.ocva.model.InstrumentoNecessario;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.model.Partitura;
import lombok.Getter;

@Getter
public class EnsaioResponseDTO {

    private final Long id;
    private final String data;
    private final String descricao;
    private final String titulo;
    private final String tipo;
    private final List<MusicoResumoDTO> musicos;
    private final List<MusicoResumoDTO> presencas;
    private final List<MusicoResumoDTO> faltas;
    private final MusicoResumoDTO criador;
    private final MusicoResumoDTO maestro;
    private final EventoResumoDTO evento;
    private final List<InstrumentoNecessarioResponseDTO> instrumentos;
    private final List<Partitura> partituras;

    public EnsaioResponseDTO(Ensaio ensaio) {
        this.id = ensaio.getId();
        this.data = ensaio.getData();
        this.descricao = ensaio.getDescricao();
        this.titulo = ensaio.getTitulo();
        this.maestro = ensaio.getMaestro() == null ? null : new MusicoResumoDTO(ensaio.getMaestro());
        this.evento = EventoResumoDTO.de(ensaio.getEventoMarcado(), ensaio.getEvento());
        this.tipo = ensaio.getTipo();
        this.musicos = converter(ensaio.getMusicos());
        this.presencas = converter(ensaio.getPresencas());
        this.faltas = converter(ensaio.getFaltas());
        this.criador = ensaio.getCriador() == null
                ? null : new MusicoResumoDTO(ensaio.getCriador());

        this.instrumentos = new ArrayList<>();
        if (ensaio.getInstrumentos() != null) {
            for (InstrumentoNecessario item : ensaio.getInstrumentos()) {
                this.instrumentos.add(new InstrumentoNecessarioResponseDTO(item, ensaio.getMusicos()));
            }
        }

        this.partituras = ensaio.getPartituras() == null ? List.of() : ensaio.getPartituras();
    }

    private static List<MusicoResumoDTO> converter(List<Musico> musicos) {
        return musicos == null ? List.of() : musicos.stream().map(MusicoResumoDTO::new).toList();
    }
}
