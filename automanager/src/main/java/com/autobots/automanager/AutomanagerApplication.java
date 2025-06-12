package com.autobots.automanager;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
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
import com.autobots.automanager.servicos.CodigoBarrasGerador;

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
        
        @Autowired
        private PasswordEncoder passwordEncoder;
        
        @Autowired
        private CodigoBarrasGerador codigoBarrasGerador;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            Calendar calendario = Calendar.getInstance();
            
            // Create company
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
            
            // Create merchandise
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
            
            // Create services
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
            
            // Save company
            empresa = empresaRepositorio.save(empresa);
            
            // Create ADMIN user (Dom Pedro with full admin rights)
            calendario.set(2002, 05, 15);
            Cliente adminCliente = new Cliente();
            adminCliente.setNome("Pedro Alcântara de Bragança e Bourbon");
            adminCliente.setDataCadastro(Calendar.getInstance().getTime());
            adminCliente.setDataNascimento(calendario.getTime());
            adminCliente.setNomeSocial("Dom Pedro");
            adminCliente.setPerfil(PerfilUsuario.ADMIN);
            adminCliente.setEmpresa(empresa);
            
            Telefone telefoneAdmin = new Telefone();
            telefoneAdmin.setDdd("21");
            telefoneAdmin.setNumero("981234576");
            adminCliente.getTelefones().add(telefoneAdmin);
            
            Endereco enderecoAdmin = new Endereco();
            enderecoAdmin.setEstado("Rio de Janeiro");
            enderecoAdmin.setCidade("Rio de Janeiro");
            enderecoAdmin.setBairro("Copacabana");
            enderecoAdmin.setRua("Avenida Atlântica");
            enderecoAdmin.setNumero("1702");
            enderecoAdmin.setCodigoPostal("22021001");
            enderecoAdmin.setInformacoesAdicionais("Hotel Copacabana palace");
            adminCliente.setEndereco(enderecoAdmin);
            
            Documento rg = new Documento();
            rg.setTipo("RG");
            rg.setNumero("1500");
            
            Documento cpf = new Documento();
            cpf.setTipo("CPF");
            cpf.setNumero("00000000001");
            
            adminCliente.getDocumentos().add(rg);
            adminCliente.getDocumentos().add(cpf);
            
            // Create ENCRYPTED credentials for admin
            CredencialUsuario credencialAdmin = new CredencialUsuario();
            credencialAdmin.setNomeUsuario("dompedro");
            credencialAdmin.setSenha(passwordEncoder.encode("imperio123"));
            credencialAdmin.setInativo(false);
            credencialAdmin.setCriacao(Calendar.getInstance().getTime());
            credencialAdmin.setDataCriacao(Calendar.getInstance().getTime());
            adminCliente.addCredencial(credencialAdmin);
            
            // Create barcode credential for admin
            CredencialCodigoBarra credencialBarcodeAdmin = new CredencialCodigoBarra();
            String codigoAdmin = codigoBarrasGerador.gerarCodigoPorPerfil("ADMIN");
            credencialBarcodeAdmin.setCodigo(codigoAdmin);
            credencialBarcodeAdmin.setCriacao(Calendar.getInstance().getTime());
            credencialBarcodeAdmin.setInativo(false);
            adminCliente.addCredencial(credencialBarcodeAdmin);
            
            // Create vehicle for admin
            Veiculo veiculo = new Veiculo();
            veiculo.setTipo(TipoVeiculo.CARRO);
            veiculo.setModelo("Mercedes-Benz S-Class");
            veiculo.setPlaca("IMP-2024");
            veiculo.setProprietario(adminCliente);
            adminCliente.getVeiculos().add(veiculo);
            
            // Create GERENTE (Manager)
            Cliente gerente = new Cliente();
            gerente.setNome("José da Silva");
            gerente.setDataCadastro(Calendar.getInstance().getTime());
            gerente.setPerfil(PerfilUsuario.GERENTE);
            gerente.setEmpresa(empresa);
            
            // Create ENCRYPTED credentials for manager
            CredencialUsuario credencialGerente = new CredencialUsuario();
            credencialGerente.setNomeUsuario("jose.silva");
            credencialGerente.setSenha(passwordEncoder.encode("gerente123"));
            credencialGerente.setInativo(false);
            credencialGerente.setCriacao(Calendar.getInstance().getTime());
            credencialGerente.setDataCriacao(Calendar.getInstance().getTime());
            gerente.addCredencial(credencialGerente);
            
            // Create barcode for manager
            CredencialCodigoBarra credencialBarcodeGerente = new CredencialCodigoBarra();
            String codigoGerente = codigoBarrasGerador.gerarCodigoPorPerfil("GERENTE");
            credencialBarcodeGerente.setCodigo(codigoGerente);
            credencialBarcodeGerente.setCriacao(Calendar.getInstance().getTime());
            credencialBarcodeGerente.setInativo(false);
            gerente.addCredencial(credencialBarcodeGerente);
            
            Endereco enderecoGerente = new Endereco();
            enderecoGerente.setEstado("São Paulo");
            enderecoGerente.setCidade("São Paulo");
            enderecoGerente.setBairro("Liberdade");
            enderecoGerente.setRua("Rua da Glória");
            enderecoGerente.setNumero("456");
            enderecoGerente.setCodigoPostal("01510000");
            gerente.setEndereco(enderecoGerente);
            
            Telefone telefoneGerente = new Telefone();
            telefoneGerente.setDdd("11");
            telefoneGerente.setNumero("987654321");
            gerente.getTelefones().add(telefoneGerente);
            
            Documento cpfGerente = new Documento();
            cpfGerente.setTipo("CPF");
            cpfGerente.setNumero("12345678900");
            gerente.getDocumentos().add(cpfGerente);
            
            // Create VENDEDOR (Salesperson)
            Cliente vendedor = new Cliente();
            vendedor.setNome("Ana Vendedora");
            vendedor.setDataCadastro(Calendar.getInstance().getTime());
            vendedor.setPerfil(PerfilUsuario.VENDEDOR);
            vendedor.setEmpresa(empresa);
            
            // Create ENCRYPTED credentials for salesperson
            CredencialUsuario credencialVendedor = new CredencialUsuario();
            credencialVendedor.setNomeUsuario("ana.vendas");
            credencialVendedor.setSenha(passwordEncoder.encode("vendas123"));
            credencialVendedor.setInativo(false);
            credencialVendedor.setCriacao(Calendar.getInstance().getTime());
            credencialVendedor.setDataCriacao(Calendar.getInstance().getTime());
            vendedor.addCredencial(credencialVendedor);
            
            // Create barcode for salesperson
            CredencialCodigoBarra credencialBarcodeVendedor = new CredencialCodigoBarra();
            String codigoVendedor = codigoBarrasGerador.gerarCodigoPorPerfil("VENDEDOR");
            credencialBarcodeVendedor.setCodigo(codigoVendedor);
            credencialBarcodeVendedor.setCriacao(Calendar.getInstance().getTime());
            credencialBarcodeVendedor.setInativo(false);
            vendedor.addCredencial(credencialBarcodeVendedor);
            
            Endereco enderecoVendedor = new Endereco();
            enderecoVendedor.setEstado("São Paulo");
            enderecoVendedor.setCidade("São Paulo");
            enderecoVendedor.setBairro("Vila Madalena");
            enderecoVendedor.setRua("Rua das Vendas");
            enderecoVendedor.setNumero("321");
            enderecoVendedor.setCodigoPostal("05435000");
            vendedor.setEndereco(enderecoVendedor);
            
            Telefone telefoneVendedor = new Telefone();
            telefoneVendedor.setDdd("11");
            telefoneVendedor.setNumero("888777666");
            vendedor.getTelefones().add(telefoneVendedor);
            
            Documento cpfVendedor = new Documento();
            cpfVendedor.setTipo("CPF");
            cpfVendedor.setNumero("11122233344");
            vendedor.getDocumentos().add(cpfVendedor);
            
            // Create a CLIENTE for testing
            Cliente clienteTeste = new Cliente();
            clienteTeste.setNome("Maria Santos");
            clienteTeste.setDataCadastro(Calendar.getInstance().getTime());
            clienteTeste.setPerfil(PerfilUsuario.CLIENTE);
            
            // Create ENCRYPTED credentials for client
            CredencialUsuario credencialCliente = new CredencialUsuario();
            credencialCliente.setNomeUsuario("maria.santos");
            credencialCliente.setSenha(passwordEncoder.encode("cliente123"));
            credencialCliente.setInativo(false);
            credencialCliente.setCriacao(Calendar.getInstance().getTime());
            credencialCliente.setDataCriacao(Calendar.getInstance().getTime());
            clienteTeste.addCredencial(credencialCliente);
            
            // Create barcode credential for client
            CredencialCodigoBarra credencialBarcodeCliente = new CredencialCodigoBarra();
            String codigoCliente = codigoBarrasGerador.gerarCodigoPorPerfil("CLIENTE");
            credencialBarcodeCliente.setCodigo(codigoCliente);
            credencialBarcodeCliente.setCriacao(Calendar.getInstance().getTime());
            credencialBarcodeCliente.setInativo(false);
            clienteTeste.addCredencial(credencialBarcodeCliente);
            
            Endereco enderecoCliente = new Endereco();
            enderecoCliente.setEstado("São Paulo");
            enderecoCliente.setCidade("São Paulo");
            enderecoCliente.setBairro("Vila Olímpia");
            enderecoCliente.setRua("Rua do Cliente");
            enderecoCliente.setNumero("789");
            enderecoCliente.setCodigoPostal("04551000");
            clienteTeste.setEndereco(enderecoCliente);
            
            Telefone telefoneCliente = new Telefone();
            telefoneCliente.setDdd("11");
            telefoneCliente.setNumero("999888777");
            clienteTeste.getTelefones().add(telefoneCliente);
            
            Documento cpfCliente = new Documento();
            cpfCliente.setTipo("CPF");
            cpfCliente.setNumero("98765432100");
            clienteTeste.getDocumentos().add(cpfCliente);
            
            // Save all clients
            clienteRepositorio.save(adminCliente);
            clienteRepositorio.save(gerente);
            clienteRepositorio.save(vendedor);
            clienteRepositorio.save(clienteTeste);
            
            // Update company with users
            empresa.getUsuarios().add(adminCliente);
            empresa.getUsuarios().add(gerente);
            empresa.getUsuarios().add(vendedor);
            empresaRepositorio.save(empresa);
            
            System.out.println("=== INITIAL DATA CREATED ===");
            System.out.println("Admin: dompedro / imperio123 (Barcode: " + codigoAdmin + ")");
            System.out.println("Manager: jose.silva / gerente123 (Barcode: " + codigoGerente + ")");
            System.out.println("Salesperson: ana.vendas / vendas123 (Barcode: " + codigoVendedor + ")");
            System.out.println("Client: maria.santos / cliente123 (Barcode: " + codigoCliente + ")");
            System.out.println("===========================");
        }
    }
}