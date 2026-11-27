package br.cefetmg.ocva.service;

import java.util.ArrayList;
import java.util.List;

import br.cefetmg.ocva.model.Musico;

/**
 * Presença padrão de ensaios e eventos.
 * - Ao registrar um encontro/evento que passou: só quem respondeu à enquete entra;
 *   "Vou" começa com presença e "Não vou" com falta. Quem não respondeu não entra.
 * - Ao cadastrar ou editar direto: todo músico escalado começa com presença.
 * A marcação pode ser trocada depois na página de detalhe.
 */
public class PresencaPadrao {

    private PresencaPadrao() {
    }

    /** Escala quem respondeu à enquete ("Vou" e "Não vou"). */
    public static List<Musico> escalados(List<Musico> vao, List<Musico> naoVao) {
        List<Musico> lista = new ArrayList<>();
        adicionarTodos(lista, vao);
        adicionarTodos(lista, naoVao);
        return lista;
    }

    /** Todos os escalados, menos quem tem falta. */
    public static List<Musico> presentes(List<Musico> escalados, List<Musico> faltas) {
        List<Musico> lista = new ArrayList<>();
        for (Musico musico : escalados) {
            if (!contem(faltas, musico)) {
                lista.add(musico);
            }
        }
        return lista;
    }

    /**
     * Ao salvar a lista de escalados de um ensaio ou evento:
     * quem entrou ganha presença e quem saiu perde a marcação.
     */
    public static void atualizar(List<Musico> escalados, List<Musico> presencas, List<Musico> faltas) {
        presencas.removeIf(musico -> !contem(escalados, musico));
        faltas.removeIf(musico -> !contem(escalados, musico));
        for (Musico musico : escalados) {
            if (!contem(presencas, musico) && !contem(faltas, musico)) {
                presencas.add(musico);
            }
        }
    }

    private static void adicionarTodos(List<Musico> lista, List<Musico> novos) {
        if (novos == null) {
            return;
        }
        for (Musico musico : novos) {
            if (!contem(lista, musico)) {
                lista.add(musico);
            }
        }
    }

    private static boolean contem(List<Musico> lista, Musico musico) {
        if (lista == null) {
            return false;
        }
        for (Musico item : lista) {
            if (item.getId().equals(musico.getId())) {
                return true;
            }
        }
        return false;
    }
}
