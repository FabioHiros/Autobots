package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Veiculo;

public class VeiculoAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Veiculo veiculo, Veiculo atualizacao) {
        if (atualizacao != null) {
            if (atualizacao.getTipo() != null) {
                veiculo.setTipo(atualizacao.getTipo());
            }
            if (!verificador.verificar(atualizacao.getModelo())) {
                veiculo.setModelo(atualizacao.getModelo());
            }
            if (!verificador.verificar(atualizacao.getPlaca())) {
                veiculo.setPlaca(atualizacao.getPlaca());
            }
            if (atualizacao.getProprietario() != null) {
                veiculo.setProprietario(atualizacao.getProprietario());
            }
        }
    }
}