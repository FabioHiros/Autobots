// Final Fixed ClienteControle.java with correct authorization permissions
package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.dto.ClienteRegistroDTO;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuario;
import com.autobots.automanager.entidades.PerfilUsuario;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.servicos.CodigoBarrasGerador;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/cliente")
public class ClienteControle {
    @Autowired
    private ClienteRepositorio repositorio;
    @Autowired
    private ClienteSelecionador selecionador;
    @Autowired
    private CodigoBarrasGerador codigoBarrasGerador;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ADMIN -> Pode ver tudo
    // Gerente pode ver tudo que não for de admin
    // Vendedor pode ver tudo de cliente e suas prorpias informações
    // cliente só pode ver suas informações
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @clienteControle.canGerenteRead(#id)) or " +
                  "(hasRole('VENDEDOR') and (@clienteControle.isClienteUser(#id) or #id == authentication.principal.cliente.id)) or " +
                  "(hasRole('CLIENTE') and #id == authentication.principal.cliente.id)")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> obterCliente(@PathVariable long id) {
        List<Cliente> clientes = repositorio.findAll();
        Cliente cliente = selecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Cliente> clienteModel = EntityModel.of(cliente);
        clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
        clienteModel.add(linkTo(methodOn(ClienteControle.class).atualizarCliente(id, new Cliente())).withRel("update"));
        clienteModel.add(linkTo(methodOn(ClienteControle.class).excluirCliente(id)).withRel("delete"));
        clienteModel.add(linkTo(ClienteControle.class).withRel("clientes"));
        
        return new ResponseEntity<>(clienteModel, HttpStatus.OK);
    }

    // só as 3 roles abaixo podem usar essa rota e ela filtra o que cade uma pode ver
    // de acordo com a role
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> obterClientes() {
        List<Cliente> clientes = repositorio.findAll();
        
      
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
             
                clientes = clientes.stream()
                    .filter(c -> c.getPerfil() != null && c.getPerfil().name().equals("CLIENTE"))
                    .collect(Collectors.toList());
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"))) {
              
                clientes = clientes.stream()
                    .filter(c -> c.getPerfil() != null && 
                            (c.getPerfil().name().equals("GERENTE") || 
                             c.getPerfil().name().equals("VENDEDOR") || 
                             c.getPerfil().name().equals("CLIENTE")))
                    .collect(Collectors.toList());
            }
           
        }
        
        List<EntityModel<Cliente>> clienteModels = clientes.stream()
            .map(cliente -> {
                EntityModel<Cliente> clienteModel = EntityModel.of(cliente);
                clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(cliente.getId())).withSelfRel());
                return clienteModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Cliente>> collectionModel = CollectionModel.of(clienteModels);
        collectionModel.add(linkTo(ClienteControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // @Operation(summary = "Public client registration - No authentication required")
    // @PostMapping("/registrar")
    // public ResponseEntity<?> registrarClientePublico(@RequestBody ClienteRegistroDTO dto) {
    //     try {
           
    //         dto.setPerfil(PerfilUsuario.CLIENTE);
            
    //         Cliente cliente = createClienteWithCredentials(dto);
    //         Cliente clienteSalvo = repositorio.save(cliente);
            
    //         EntityModel<Cliente> clienteModel = EntityModel.of(clienteSalvo);
    //         clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteSalvo.getId())).withSelfRel());
            
    //         return new ResponseEntity<>(clienteModel, HttpStatus.CREATED);
    //     } catch (Exception e) {
    //         return new ResponseEntity<>("Erro ao registrar cliente: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    //     }
    // }

    // Admin pode criar todos
    // Gerente só prode criar de gerente pra baixo
    // vendedor só cria clientes
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @clienteControle.canGerenteCreate(#dto.perfil)) or " +
                  "(hasRole('VENDEDOR') and @clienteControle.canVendedorCreate(#dto.perfil))")
    @PostMapping
    public ResponseEntity<EntityModel<Cliente>> criarCliente(@RequestBody ClienteRegistroDTO dto) {
        try {
            Cliente cliente = createClienteWithCredentials(dto);
            Cliente clienteSalvo = repositorio.save(cliente);
            
            EntityModel<Cliente> clienteModel = EntityModel.of(clienteSalvo);
            clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteSalvo.getId())).withSelfRel());
            
            return new ResponseEntity<>(clienteModel, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // admin pode atualizar todos
    // gerente pode atualizar todos menos admin
    // vendedor só atualiza cliente
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @clienteControle.canGerenteUpdate(#id)) or " +
                  "(hasRole('VENDEDOR') and (@clienteControle.isClienteUser(#id)))")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> atualizarCliente(@PathVariable long id, @RequestBody Cliente atualizacao) {
        if (repositorio.existsById(id)) {
            Cliente cliente = repositorio.getById(id);
            ClienteAtualizador atualizador = new ClienteAtualizador();
            atualizador.atualizar(cliente, atualizacao);
            Cliente clienteAtualizado = repositorio.save(cliente);
            
            EntityModel<Cliente> clienteModel = EntityModel.of(clienteAtualizado);
            clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
            
            return new ResponseEntity<>(clienteModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // admin deleta de admin pra baixo
    // gerente deleta de gerente pra baixo
    // vendedor só deleta cliente
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @clienteControle.canGerenteDelete(#id)) or " +
                  "(hasRole('VENDEDOR') and @clienteControle.isClienteUser(#id))")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCliente(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Cliente cliente = repositorio.getById(id);
            repositorio.delete(cliente);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    private Cliente createClienteWithCredentials(ClienteRegistroDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setNomeSocial(dto.getNomeSocial());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setPerfil(dto.getPerfil());
        cliente.setDataCadastro(new Date());
        cliente.setEndereco(dto.getEndereco());
        cliente.setDocumentos(dto.getDocumentos());
        cliente.setTelefones(dto.getTelefones());

        CredencialUsuario credencialUsuario = new CredencialUsuario();
        credencialUsuario.setNomeUsuario(dto.getNomeUsuario());
        credencialUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        credencialUsuario.setCriacao(new Date());
        credencialUsuario.setDataCriacao(new Date());
        credencialUsuario.setInativo(false);

        CredencialCodigoBarra credencialBarcode = new CredencialCodigoBarra();
        String codigoGerado = codigoBarrasGerador.gerarCodigoPorPerfil(
            cliente.getPerfil() != null ? cliente.getPerfil().toString() : "CLIENTE"
        );
        credencialBarcode.setCodigo(codigoGerado);
        credencialBarcode.setCriacao(new Date());
        credencialBarcode.setInativo(false);

        cliente.addCredencial(credencialUsuario);
        cliente.addCredencial(credencialBarcode);

        return cliente;
    }

    public boolean isClienteUser(long clienteId) {
        Cliente cliente = repositorio.findById(clienteId).orElse(null);
        return cliente != null && cliente.getPerfil() != null && 
               cliente.getPerfil().name().equals("CLIENTE");
    }
    
    public boolean canGerenteRead(long clienteId) {
        Cliente cliente = repositorio.findById(clienteId).orElse(null);
        if (cliente == null || cliente.getPerfil() == null) return false;
        String perfil = cliente.getPerfil().name();
        return perfil.equals("GERENTE") || perfil.equals("VENDEDOR") || perfil.equals("CLIENTE");
    }
    
    public boolean canGerenteCreate(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.GERENTE || 
               perfil == PerfilUsuario.VENDEDOR || 
               perfil == PerfilUsuario.CLIENTE;
    }
    
    public boolean canGerenteUpdate(long clienteId) {
        return canGerenteRead(clienteId);
    }
    
    public boolean canGerenteDelete(long clienteId) {
        return canGerenteRead(clienteId);
    }
    
    public boolean canVendedorCreate(PerfilUsuario perfil) {
        return perfil == PerfilUsuario.CLIENTE;
    }
}