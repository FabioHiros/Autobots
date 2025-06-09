package com.autobots.automanager;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialUsuario;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.PerfilUsuario;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.TipoVeiculo;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@SpringBootApplication
public class AutomanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomanagerApplication.class, args);
    }

    @Component
    public static class Runner implements ApplicationRunner {
        @Autowired
        public ClienteRepositorio clienteRepositorio;
        
        @Autowired
        public EmpresaRepositorio empresaRepositorio;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            Calendar calendario = Calendar.getInstance();
            
           
            Empresa empresa = new Empresa();
            empresa.setRazaoSocial("AutoBots Ltda");
            empresa.setNomeFantasia("AutoBots");
            empresa.setDataCadastro(Calendar.getInstance().getTime());
            
            Endereco enderecoEmpresa = new Endereco();
            enderecoEmpresa.setEstado("São Paulo");
            enderecoEmpresa.setCidade("São Paulo");
            enderecoEmpresa.setBairro("Vila Madalena");
            enderecoEmpresa.setRua("Rua Harmonia");
            enderecoEmpresa.setNumero("123");
            enderecoEmpresa.setCodigoPostal("05435000");
            empresa.setEndereco(enderecoEmpresa);
            
            Telefone telefoneEmpresa = new Telefone();
            telefoneEmpresa.setDdd("11");
            telefoneEmpresa.setNumero("30001234");
            empresa.getTelefones().add(telefoneEmpresa);
            
           
            Mercadoria oleo = new Mercadoria();
            oleo.setNome("Óleo de Motor 5W30");
            oleo.setDescricao("Óleo sintético para motores");
            oleo.setValor(89.90);
            oleo.setFabricante("Castrol");
            oleo.setQuantidadeEstoque(50);
            oleo.setEmpresa(empresa);
            empresa.getMercadorias().add(oleo);
            
            Mercadoria filtro = new Mercadoria();
            filtro.setNome("Filtro de Óleo");
            filtro.setDescricao("Filtro de óleo universal");
            filtro.setValor(25.50);
            filtro.setFabricante("Mann Filter");
            filtro.setQuantidadeEstoque(100);
            filtro.setEmpresa(empresa);
            empresa.getMercadorias().add(filtro);
            
            
            Servico trocaOleo = new Servico();
            trocaOleo.setNome("Troca de Óleo");
            trocaOleo.setDescricao("Troca completa de óleo do motor");
            trocaOleo.setValor(120.00);
            trocaOleo.setEmpresa(empresa);
            empresa.getServicos().add(trocaOleo);
            
            Servico revisao = new Servico();
            revisao.setNome("Revisão Completa");
            revisao.setDescricao("Revisão geral do veículo");
            revisao.setValor(350.00);
            revisao.setEmpresa(empresa);
            empresa.getServicos().add(revisao);
            
           
            empresa = empresaRepositorio.save(empresa);
            
         
            calendario.set(2002, 05, 15);
            Cliente cliente = new Cliente();
            cliente.setNome("Pedro Alcântara de Bragança e Bourbon");
            cliente.setDataCadastro(Calendar.getInstance().getTime());
            cliente.setDataNascimento(calendario.getTime());
            cliente.setNomeSocial("Dom Pedro");
            cliente.setPerfil(PerfilUsuario.CLIENTE);
            cliente.setEmpresa(empresa);
            
            Telefone telefone = new Telefone();
            telefone.setDdd("21");
            telefone.setNumero("981234576");
            cliente.getTelefones().add(telefone);
            
            Endereco endereco = new Endereco();
            endereco.setEstado("Rio de Janeiro");
            endereco.setCidade("Rio de Janeiro");
            endereco.setBairro("Copacabana");
            endereco.setRua("Avenida Atlântica");
            endereco.setNumero("1702");
            endereco.setCodigoPostal("22021001");
            endereco.setInformacoesAdicionais("Hotel Copacabana palace");
            cliente.setEndereco(endereco);
            
            Documento rg = new Documento();
            rg.setTipo("RG");
            rg.setNumero("1500");
            
            Documento cpf = new Documento();
            cpf.setTipo("CPF");
            cpf.setNumero("00000000001");
            
            cliente.getDocumentos().add(rg);
            cliente.getDocumentos().add(cpf);
            
            
            CredencialUsuario credencial = new CredencialUsuario();
            credencial.setNomeUsuario("dompedro");
            credencial.setSenha("imperio123");
            credencial.setInativo(false);
            credencial.setCriacao(Calendar.getInstance().getTime());
            credencial.setDataCriacao(Calendar.getInstance().getTime());
            cliente.addCredencial(credencial);
            
          
            Veiculo veiculo = new Veiculo();
            veiculo.setTipo(TipoVeiculo.CARRO);
            veiculo.setModelo("Mercedes-Benz S-Class");
            veiculo.setPlaca("IMP-2024");
            veiculo.setProprietario(cliente);
            cliente.getVeiculos().add(veiculo);
            
       
            Cliente funcionario = new Cliente();
            funcionario.setNome("José da Silva");
            funcionario.setDataCadastro(Calendar.getInstance().getTime());
            funcionario.setPerfil(PerfilUsuario.FUNCIONARIO);
            funcionario.setEmpresa(empresa);
            
          
            CredencialUsuario credencialFunc = new CredencialUsuario();
            credencialFunc.setNomeUsuario("jose.silva");
            credencialFunc.setSenha("func123");
            credencialFunc.setInativo(false);
            credencialFunc.setCriacao(Calendar.getInstance().getTime());
            credencialFunc.setDataCriacao(Calendar.getInstance().getTime());
            funcionario.addCredencial(credencialFunc);
            
            Endereco enderecoFunc = new Endereco();
            enderecoFunc.setEstado("São Paulo");
            enderecoFunc.setCidade("São Paulo");
            enderecoFunc.setBairro("Liberdade");
            enderecoFunc.setRua("Rua da Glória");
            enderecoFunc.setNumero("456");
            enderecoFunc.setCodigoPostal("01510000");
            funcionario.setEndereco(enderecoFunc);
            
            Telefone telefoneFunc = new Telefone();
            telefoneFunc.setDdd("11");
            telefoneFunc.setNumero("987654321");
            funcionario.getTelefones().add(telefoneFunc);
            
            Documento cpfFunc = new Documento();
            cpfFunc.setTipo("CPF");
            cpfFunc.setNumero("12345678900");
            funcionario.getDocumentos().add(cpfFunc);
            
          
            clienteRepositorio.save(cliente);
            clienteRepositorio.save(funcionario);
            
           
            empresa.getUsuarios().add(cliente);
            empresa.getUsuarios().add(funcionario);
            empresaRepositorio.save(empresa);
        }
    }
}