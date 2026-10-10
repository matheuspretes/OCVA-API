package br.cefetmg.ocva.dto;

import lombok.Getter;

@Getter
public class CodigoAcessoVerificacaoResponseDTO {

    private final boolean disponivel;
    private final String mensagem;

    public CodigoAcessoVerificacaoResponseDTO(boolean disponivel, String mensagem) {
        this.disponivel = disponivel;
        this.mensagem = mensagem;
    }
}
