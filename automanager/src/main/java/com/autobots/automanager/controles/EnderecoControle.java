package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
@RequestMapping("/endereco")
public class EnderecoControle {
    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private EnderecoSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Endereco>> obterEndereco(@PathVariable long id) {
        List<Endereco> enderecos = repositorio.findAll();
        Endereco endereco = selecionador.selecionar(enderecos, id);
        if (endereco == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Endereco> enderecoModel = EntityModel.of(endereco);
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(id, null)).withRel("update"));
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(id)).withRel("delete"));
        enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        
        return new ResponseEntity<>(enderecoModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Endereco>>> obterEnderecos() {
        List<Endereco> enderecos = repositorio.findAll();
        
        List<EntityModel<Endereco>> enderecoModels = enderecos.stream()
            .map(endereco -> {
                EntityModel<Endereco> enderecoModel = EntityModel.of(endereco);
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(endereco.getId())).withSelfRel());
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(endereco.getId(), null)).withRel("update"));
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(endereco.getId())).withRel("delete"));
                return enderecoModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Endereco>> collectionModel = CollectionModel.of(enderecoModels);
        collectionModel.add(linkTo(EnderecoControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(EnderecoControle.class).cadastrarEndereco(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Endereco>> cadastrarEndereco(@RequestBody Endereco endereco) {
        Endereco enderecoSalvo = repositorio.save(endereco);
        
        EntityModel<Endereco> enderecoModel = EntityModel.of(enderecoSalvo);
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(enderecoSalvo.getId())).withSelfRel());
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(enderecoSalvo.getId(), null)).withRel("update"));
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(enderecoSalvo.getId())).withRel("delete"));
        enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        
        return new ResponseEntity<>(enderecoModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Endereco>> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
        if (repositorio.existsById(id)) {
            Endereco endereco = repositorio.findById(id).get();
            EnderecoAtualizador atualizador = new EnderecoAtualizador();
            atualizador.atualizar(endereco, atualizacao);
            Endereco enderecoAtualizado = repositorio.save(endereco);
            
            EntityModel<Endereco> enderecoModel = EntityModel.of(enderecoAtualizado);
            enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
            enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(id)).withRel("delete"));
            enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
            
            return new ResponseEntity<>(enderecoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
        if (!repositorio.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Remove address from all clients first
        List<Cliente> clientes = clienteRepositorio.findAll();
        boolean wasRemoved = false;
        for (Cliente cliente : clientes) {
            if (cliente.getEndereco() != null && cliente.getEndereco().getId().equals(id)) {
                cliente.setEndereco(null);
                clienteRepositorio.save(cliente); // This triggers orphanRemoval and deletes the address
                wasRemoved = true;
            }
        }
        
        // Only delete manually if it wasn't removed from any client
        if (!wasRemoved && repositorio.existsById(id)) {
            repositorio.deleteById(id);
        }
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}