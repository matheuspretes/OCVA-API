package br.cefetmg.ocva.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.cefetmg.ocva.model.InstrumentoNecessario;

/**
 * Regras usadas no cadastro de ensaios e eventos.
 */
public class AgendaValidador {

    public static final int MESES_MAXIMO_ANTECEDENCIA = 12;

    private AgendaValidador() {
    }

    /**
     * A data deve estar no formato "aaaa-mm-ddThh:mm:ss",
     * não pode estar no passado e não pode passar de 12 meses à frente.
     */
    public static void validarData(String data) {
        if (data == null || data.isBlank()) {
            throw erro("A data é obrigatória.");
        }

        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(data);
        } catch (DateTimeParseException e) {
            throw erro("Data inválida. Use o formato aaaa-mm-ddThh:mm:ss.");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (dataHora.isBefore(agora)) {
            throw erro("Não é possível cadastrar com uma data que já passou.");
        }
        if (dataHora.isAfter(agora.plusMonths(MESES_MAXIMO_ANTECEDENCIA))) {
            throw erro("A data não pode ser mais de " + MESES_MAXIMO_ANTECEDENCIA + " meses à frente.");
        }
    }

    /**
     * Retorna apenas o dia (aaaa-mm-dd) de uma data "aaaa-mm-ddThh:mm:ss".
     */
    public static String pegarDia(String data) {
        return data.substring(0, 10);
    }

    /**
     * Confere a lista de instrumentos necessários e devolve uma cópia limpa.
     */
    public static List<InstrumentoNecessario> validarInstrumentos(List<InstrumentoNecessario> lista) {
        List<InstrumentoNecessario> resultado = new ArrayList<>();
        if (lista == null) {
            return resultado;
        }

        List<String> jaAdicionados = new ArrayList<>();
        for (InstrumentoNecessario item : lista) {
            if (item == null || item.getInstrumento() == null || item.getInstrumento().isBlank()) {
                throw erro("Informe o nome do instrumento.");
            }
            if (item.getQuantidade() == null || item.getQuantidade() < 1) {
                throw erro("A quantidade de músicos para " + item.getInstrumento() + " deve ser maior que zero.");
            }

            String nome = item.getInstrumento().trim();
            if (jaAdicionados.contains(nome.toLowerCase())) {
                throw erro("O instrumento " + nome + " foi informado mais de uma vez.");
            }
            jaAdicionados.add(nome.toLowerCase());
            resultado.add(new InstrumentoNecessario(nome, item.getQuantidade()));
        }
        return resultado;
    }

    private static ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
