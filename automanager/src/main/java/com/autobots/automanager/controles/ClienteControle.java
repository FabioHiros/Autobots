package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController
@RequestMapping("/cliente")
public class ClienteControle {
    @Autowired
    private ClienteRepositorio repositorio;
    @Autowired
    private ClienteSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> obterCliente(@PathVariable long id) {
        List<Cliente> clientes = repositorio.findAll();
        Cliente cliente = selecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
       
        EntityModel<Cliente> clienteModel = EntityModel.of(cliente);
        
       
        clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
        
        
        clienteModel.add(linkTo(methodOn(ClienteControle.class).atualizarCliente(id, null)).withRel("update"));
        clienteModel.add(linkTo(methodOn(ClienteControle.class).excluirCliente(id)).withRel("delete"));
        
  
        clienteModel.add(linkTo(ClienteControle.class).withRel("clientes"));
        
       
        if (cliente.getEndereco() != null) {
            clienteModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(cliente.getEndereco().getId())).withRel("endereco"));
        }
        
        if (!cliente.getTelefones().isEmpty()) {
            clienteModel.add(linkTo(ClienteControle.class).slash(id).slash("telefones").withRel("telefones"));
        }
        
        if (!cliente.getDocumentos().isEmpty()) {
            clienteModel.add(linkTo(ClienteControle.class).slash(id).slash("documentos").withRel("documentos"));
        }
        
        return new ResponseEntity<>(clienteModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> obterClientes() {
        List<Cliente> clientes = repositorio.findAll();
        
     
        List<EntityModel<Cliente>> clienteModels = clientes.stream()
            .map(cliente -> {
                EntityModel<Cliente> clienteModel = EntityModel.of(cliente);
                clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(cliente.getId())).withSelfRel());
                clienteModel.add(linkTo(methodOn(ClienteControle.class).atualizarCliente(cliente.getId(), null)).withRel("update"));
                clienteModel.add(linkTo(methodOn(ClienteControle.class).excluirCliente(cliente.getId())).withRel("delete"));
                return clienteModel;
            })
            .collect(Collectors.toList());
        
      
        CollectionModel<EntityModel<Cliente>> collectionModel = CollectionModel.of(clienteModels);
        collectionModel.add(linkTo(ClienteControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(ClienteControle.class).cadastrarCliente(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Cliente>> cadastrarCliente(@RequestBody Cliente cliente) {
        Cliente clienteSalvo = repositorio.save(cliente);
        
       
        EntityModel<Cliente> clienteModel = EntityModel.of(clienteSalvo);
        clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteSalvo.getId())).withSelfRel());
        clienteModel.add(linkTo(methodOn(ClienteControle.class).atualizarCliente(clienteSalvo.getId(), null)).withRel("update"));
        clienteModel.add(linkTo(methodOn(ClienteControle.class).excluirCliente(clienteSalvo.getId())).withRel("delete"));
        clienteModel.add(linkTo(ClienteControle.class).withRel("clientes"));
        
        return new ResponseEntity<>(clienteModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> atualizarCliente(@PathVariable long id, @RequestBody Cliente atualizacao) {
        if (repositorio.existsById(id)) {
            Cliente cliente = repositorio.getById(id);
            ClienteAtualizador atualizador = new ClienteAtualizador();
            atualizador.atualizar(cliente, atualizacao);
            Cliente clienteAtualizado = repositorio.save(cliente);
            
      
            EntityModel<Cliente> clienteModel = EntityModel.of(clienteAtualizado);
            clienteModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withSelfRel());
            clienteModel.add(linkTo(methodOn(ClienteControle.class).excluirCliente(id)).withRel("delete"));
            clienteModel.add(linkTo(ClienteControle.class).withRel("clientes"));
            
            return new ResponseEntity<>(clienteModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

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
    
  
    @GetMapping("/{id}/telefones")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterTelefonesCliente(@PathVariable long id) {
        List<Cliente> clientes = repositorio.findAll();
        Cliente cliente = selecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> telefoneModels = cliente.getTelefones().stream()
            .map(telefone -> EntityModel.of((Object) telefone)
                .add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefone.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(telefoneModels);
        collectionModel.add(linkTo(methodOn(ClienteControle.class).obterTelefonesCliente(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withRel("cliente"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    
    @GetMapping("/{id}/documentos")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterDocumentosCliente(@PathVariable long id) {
        List<Cliente> clientes = repositorio.findAll();
        Cliente cliente = selecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> documentoModels = cliente.getDocumentos().stream()
            .map(documento -> EntityModel.of((Object) documento)
                .add(linkTo(methodOn(DocumentoControle.class).obterDocumento(documento.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(documentoModels);
        collectionModel.add(linkTo(methodOn(ClienteControle.class).obterDocumentosCliente(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(id)).withRel("cliente"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
}