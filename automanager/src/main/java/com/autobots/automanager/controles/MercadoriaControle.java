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

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.modelo.MercadoriaSelecionador;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;

@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {
    @Autowired
    private MercadoriaRepositorio repositorio;
    @Autowired
    private MercadoriaSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Mercadoria>> obterMercadoria(@PathVariable long id) {
        List<Mercadoria> mercadorias = repositorio.findAll();
        Mercadoria mercadoria = selecionador.selecionar(mercadorias, id);
        
        if (mercadoria == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Mercadoria> mercadoriaModel = EntityModel.of(mercadoria);
        
        // Self link
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(id)).withSelfRel());
        
        // CRUD links
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).atualizarMercadoria(id, null)).withRel("update"));
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).excluirMercadoria(id)).withRel("delete"));
        
        // Collection link
        mercadoriaModel.add(linkTo(MercadoriaControle.class).withRel("mercadorias"));
        
        // Related entities links
        if (mercadoria.getEmpresa() != null) {
            mercadoriaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(mercadoria.getEmpresa().getId())).withRel("empresa"));
        }
        
        return new ResponseEntity<>(mercadoriaModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Mercadoria>>> obterMercadorias() {
        List<Mercadoria> mercadorias = repositorio.findAll();
        
        List<EntityModel<Mercadoria>> mercadoriaModels = mercadorias.stream()
            .map(mercadoria -> {
                EntityModel<Mercadoria> mercadoriaModel = EntityModel.of(mercadoria);
                mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(mercadoria.getId())).withSelfRel());
                mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).atualizarMercadoria(mercadoria.getId(), null)).withRel("update"));
                mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).excluirMercadoria(mercadoria.getId())).withRel("delete"));
                return mercadoriaModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Mercadoria>> collectionModel = CollectionModel.of(mercadoriaModels);
        collectionModel.add(linkTo(MercadoriaControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(MercadoriaControle.class).cadastrarMercadoria(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Mercadoria>> cadastrarMercadoria(@RequestBody Mercadoria mercadoria) {
        Mercadoria mercadoriaSalva = repositorio.save(mercadoria);
        
        EntityModel<Mercadoria> mercadoriaModel = EntityModel.of(mercadoriaSalva);
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(mercadoriaSalva.getId())).withSelfRel());
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).atualizarMercadoria(mercadoriaSalva.getId(), null)).withRel("update"));
        mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).excluirMercadoria(mercadoriaSalva.getId())).withRel("delete"));
        mercadoriaModel.add(linkTo(MercadoriaControle.class).withRel("mercadorias"));
        
        return new ResponseEntity<>(mercadoriaModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Mercadoria>> atualizarMercadoria(@PathVariable long id, @RequestBody Mercadoria atualizacao) {
        if (repositorio.existsById(id)) {
            Mercadoria mercadoria = repositorio.getById(id);
            MercadoriaAtualizador atualizador = new MercadoriaAtualizador();
            atualizador.atualizar(mercadoria, atualizacao);
            Mercadoria mercadoriaAtualizada = repositorio.save(mercadoria);
            
            EntityModel<Mercadoria> mercadoriaModel = EntityModel.of(mercadoriaAtualizada);
            mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(id)).withSelfRel());
            mercadoriaModel.add(linkTo(methodOn(MercadoriaControle.class).excluirMercadoria(id)).withRel("delete"));
            mercadoriaModel.add(linkTo(MercadoriaControle.class).withRel("mercadorias"));
            
            return new ResponseEntity<>(mercadoriaModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMercadoria(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Mercadoria mercadoria = repositorio.getById(id);
            repositorio.delete(mercadoria);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}