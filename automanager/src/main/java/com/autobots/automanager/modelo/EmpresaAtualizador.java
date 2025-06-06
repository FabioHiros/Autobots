package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Empresa;

public class EmpresaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();
    private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
    private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();

    public void atualizar(Empresa empresa, Empresa atualizacao) {
        if (atualizacao != null) {
            if (!verificador.verificar(atualizacao.getRazaoSocial())) {
                empresa.setRazaoSocial(atualizacao.getRazaoSocial());
            }
            if (!verificador.verificar(atualizacao.getNomeFantasia())) {
                empresa.setNomeFantasia(atualizacao.getNomeFantasia());
            }
            if (atualizacao.getDataCadastro() != null) {
                empresa.setDataCadastro(atualizacao.getDataCadastro());
            }
            if (atualizacao.getEndereco() != null) {
                enderecoAtualizador.atualizar(empresa.getEndereco(), atualizacao.getEndereco());
            }
            if (atualizacao.getTelefones() != null && !atualizacao.getTelefones().isEmpty()) {
                telefoneAtualizador.atualizar(empresa.getTelefones(), atualizacao.getTelefones());
            }
        }
    }
}