package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Cliente;

public class ClienteAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();
    private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
    private DocumentoAtualizador documentoAtualizador = new DocumentoAtualizador();
    private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();

    private void atualizarDados(Cliente cliente, Cliente atualizacao) {
        if (!verificador.verificar(atualizacao.getNome())) {
            cliente.setNome(atualizacao.getNome());
        }
        if (!verificador.verificar(atualizacao.getNomeSocial())) {
            cliente.setNomeSocial(atualizacao.getNomeSocial());
        }
        if (!(atualizacao.getDataCadastro() == null)) {
            cliente.setDataCadastro(atualizacao.getDataCadastro());
        }
        if (!(atualizacao.getDataNascimento() == null)) {
            cliente.setDataNascimento(atualizacao.getDataNascimento());
        }
        if (atualizacao.getPerfil() != null) {
            cliente.setPerfil(atualizacao.getPerfil());
        }
        if (atualizacao.getEmpresa() != null) {
            cliente.setEmpresa(atualizacao.getEmpresa());
        }
        if (atualizacao.getCredencial() != null) {
            cliente.setCredencial(atualizacao.getCredencial());
        }
    }

    public void atualizar(Cliente cliente, Cliente atualizacao) {
        atualizarDados(cliente, atualizacao);
        if (cliente.getEndereco() != null && atualizacao.getEndereco() != null) {
            enderecoAtualizador.atualizar(cliente.getEndereco(), atualizacao.getEndereco());
        }
        if (atualizacao.getDocumentos() != null && !atualizacao.getDocumentos().isEmpty()) {
            documentoAtualizador.atualizar(cliente.getDocumentos(), atualizacao.getDocumentos());
        }
        if (atualizacao.getTelefones() != null && !atualizacao.getTelefones().isEmpty()) {
            telefoneAtualizador.atualizar(cliente.getTelefones(), atualizacao.getTelefones());
        }
    }
}