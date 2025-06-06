package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Mercadoria;

public class MercadoriaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Mercadoria mercadoria, Mercadoria atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getNome())) {
                mercadoria.setNome(atualizacao.getNome());
            }
            if (!verificador.verificar(atualizacao.getDescricao())) {
                mercadoria.setDescricao(atualizacao.getDescricao());
            }
            if (atualizacao.getValor() != null) {
                mercadoria.setValor(atualizacao.getValor());
            }
            if (!verificador.verificar(atualizacao.getFabricante())) {
                mercadoria.setFabricante(atualizacao.getFabricante());
            }
            if (atualizacao.getQuantidadeEstoque() != null) {
                mercadoria.setQuantidadeEstoque(atualizacao.getQuantidadeEstoque());
            }
            if (atualizacao.getEmpresa() != null) {
                mercadoria.setEmpresa(atualizacao.getEmpresa());
            }
        }
    }
}