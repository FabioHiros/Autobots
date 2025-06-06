package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Venda;

public class VendaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Venda venda, Venda atualizacao) {
        if (atualizacao != null) {
            if (atualizacao.getCadastro() != null) {
                venda.setCadastro(atualizacao.getCadastro());
            }
            if (!verificador.verificar(atualizacao.getIdentificacao())) {
                venda.setIdentificacao(atualizacao.getIdentificacao());
            }
            if (atualizacao.getCliente() != null) {
                venda.setCliente(atualizacao.getCliente());
            }
            if (atualizacao.getFuncionario() != null) {
                venda.setFuncionario(atualizacao.getFuncionario());
            }
            if (atualizacao.getMercadoria() != null) {
                venda.setMercadoria(atualizacao.getMercadoria());
            }
            if (atualizacao.getServico() != null) {
                venda.setServico(atualizacao.getServico());
            }
            if (atualizacao.getVeiculo() != null) {
                venda.setVeiculo(atualizacao.getVeiculo());
            }
            if (atualizacao.getEmpresa() != null) {
                venda.setEmpresa(atualizacao.getEmpresa());
            }
        }
    }
}