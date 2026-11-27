package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.Evento;
import br.cefetmg.ocva.model.EventoMarcado;
import br.cefetmg.ocva.model.Musico;
import br.cefetmg.ocva.repository.EventoMarcadoRepository;
import br.cefetmg.ocva.repository.EventoRepository;
import br.cefetmg.ocva.repository.MusicoRepository;

/**
 * Busca e valida o maestro responsável, os músicos escalados e o evento
 * ligado a um ensaio. Usado por encontros, ensaios e eventos.
 */
@Component
public class AgendaReferencias {

    private final MusicoRepository musicoRepository;
    private final EventoMarcadoRepository eventoMarcadoRepository;
    private final EventoRepository eventoRepository;

    public AgendaReferencias(MusicoRepository musicoRepository, EventoMarcadoRepository eventoMarcadoRepository,
            EventoRepository eventoRepository) {
        this.musicoRepository = musicoRepository;
        this.eventoMarcadoRepository = eventoMarcadoRepository;
        this.eventoRepository = eventoRepository;
    }

    /**
     * O maestro é obrigatório, precisa ser do tipo "maestro" e estar ativo.
     * Na edição, o maestro que já estava salvo é aceito mesmo se tiver sido desativado depois.
     */
    public Musico buscarMaestro(Long maestroId, Musico maestroAtual) {
        if (maestroId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione o maestro responsável.");
        }
        if (maestroAtual != null && maestroId.equals(maestroAtual.getId())) {
            return maestroAtual;
        }

        Musico maestro = musicoRepository.findById(maestroId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Maestro não encontrado."));
        if (!"maestro".equalsIgnoreCase(maestro.getTipo())) {
            throw erro(HttpStatus.BAD_REQUEST, maestro.getNome() + " não é maestro.");
        }
        if (!maestro.estaAtivo()) {
            throw erro(HttpStatus.BAD_REQUEST, "O maestro " + maestro.getNome() + " está desativado.");
        }
        return maestro;
    }

    /**
     * Monta a lista de músicos escalados.
     * Músicos desativados não podem ser adicionados, mas quem já estava na lista continua.
     */
    public List<Musico> buscarMusicos(List<Long> ids, List<Musico> atuais) {
        List<Musico> musicos = new ArrayList<>();
        if (ids == null) {
            return musicos;
        }

        for (Long id : ids) {
            if (id == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Todo músico deve possuir id");
            }
            Musico musico = musicoRepository.findById(id)
                    .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Músico com id: " + id + " não encontrado"));
            if (!musico.estaAtivo() && !contem(atuais, id)) {
                throw erro(HttpStatus.BAD_REQUEST, "O músico " + musico.getNome() + " está desativado.");
            }
            musicos.add(musico);
        }
        return musicos;
    }

    /** Todo ensaio precisa estar ligado a um evento (marcado ou já realizado). */
    public void validarEventoDoEnsaio(Long eventoMarcadoId, Long eventoId) {
        if (eventoMarcadoId == null && eventoId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione o evento deste ensaio.");
        }
        if (eventoMarcadoId != null && eventoId != null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione apenas um evento.");
        }
    }

    public EventoMarcado buscarEventoMarcado(Long id) {
        if (id == null) {
            return null;
        }
        return eventoMarcadoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Evento não encontrado."));
    }

    public Evento buscarEvento(Long id) {
        if (id == null) {
            return null;
        }
        return eventoRepository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Evento não encontrado."));
    }

    private static boolean contem(List<Musico> lista, Long id) {
        if (lista == null) {
            return false;
        }
        for (Musico musico : lista) {
            if (id.equals(musico.getId())) {
                return true;
            }
        }
        return false;
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
