package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Servico;

public class ServicoAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Servico servico, Servico atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getNome())) {
                servico.setNome(atualizacao.getNome());
            }
            if (!verificador.verificar(atualizacao.getDescricao())) {
                servico.setDescricao(atualizacao.getDescricao());
            }
            if (atualizacao.getValor() != null) {
                servico.setValor(atualizacao.getValor());
            }
            if (atualizacao.getEmpresa() != null) {
                servico.setEmpresa(atualizacao.getEmpresa());
            }
        }
    }
}
