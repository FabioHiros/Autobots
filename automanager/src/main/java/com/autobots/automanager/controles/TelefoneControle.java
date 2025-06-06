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

import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.modelo.TelefoneSelecionador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
@RequestMapping("/telefone")
public class TelefoneControle {
    @Autowired
    private TelefoneRepositorio repositorio;
    @Autowired
    private TelefoneSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Telefone>> obterTelefone(@PathVariable long id) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, id);
        
        if (telefone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Telefone> telefoneModel = EntityModel.of(telefone);
        
    
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
        
       
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(id, null)).withRel("update"));
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(id)).withRel("delete"));
        
     
        telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
        return new ResponseEntity<>(telefoneModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Telefone>>> obterTelefones() {
        List<Telefone> telefones = repositorio.findAll();
        
        List<EntityModel<Telefone>> telefoneModels = telefones.stream()
            .map(telefone -> {
                EntityModel<Telefone> telefoneModel = EntityModel.of(telefone);
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefone.getId())).withSelfRel());
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(telefone.getId(), null)).withRel("update"));
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(telefone.getId())).withRel("delete"));
                return telefoneModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Telefone>> collectionModel = CollectionModel.of(telefoneModels);
        collectionModel.add(linkTo(TelefoneControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(TelefoneControle.class).cadastrarTelefone(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Telefone>> cadastrarTelefone(@RequestBody Telefone telefone) {
        Telefone telefoneSalvo = repositorio.save(telefone);
        
        EntityModel<Telefone> telefoneModel = EntityModel.of(telefoneSalvo);
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefoneSalvo.getId())).withSelfRel());
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(telefoneSalvo.getId(), null)).withRel("update"));
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(telefoneSalvo.getId())).withRel("delete"));
        telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
        return new ResponseEntity<>(telefoneModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Telefone>> atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao) {
        if (repositorio.existsById(id)) {
            Telefone telefone = repositorio.getById(id);
            TelefoneAtualizador atualizador = new TelefoneAtualizador();
            atualizador.atualizar(telefone, atualizacao);
            Telefone telefoneAtualizado = repositorio.save(telefone);
            
            EntityModel<Telefone> telefoneModel = EntityModel.of(telefoneAtualizado);
            telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
            telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(id)).withRel("delete"));
            telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
            
            return new ResponseEntity<>(telefoneModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Telefone telefone = repositorio.getById(id);
            repositorio.delete(telefone);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}