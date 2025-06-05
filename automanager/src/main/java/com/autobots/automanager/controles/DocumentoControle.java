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

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.DocumentoSelecionador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
    @Autowired
    private DocumentoRepositorio repositorio;
    @Autowired
    private DocumentoSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Documento>> obterDocumento(@PathVariable long id) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, id);
        
        if (documento == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        
        EntityModel<Documento> documentoModel = EntityModel.of(documento);
        
   
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
        
       
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(id, null)).withRel("update"));
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(id)).withRel("delete"));
        
       
        documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
        
        return new ResponseEntity<>(documentoModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Documento>>> obterDocumentos() {
        List<Documento> documentos = repositorio.findAll();
        
      
        List<EntityModel<Documento>> documentoModels = documentos.stream()
            .map(documento -> {
                EntityModel<Documento> documentoModel = EntityModel.of(documento);
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(documento.getId())).withSelfRel());
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(documento.getId(), null)).withRel("update"));
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(documento.getId())).withRel("delete"));
                return documentoModel;
            })
            .collect(Collectors.toList());
        
       
        CollectionModel<EntityModel<Documento>> collectionModel = CollectionModel.of(documentoModels);
        collectionModel.add(linkTo(DocumentoControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(DocumentoControle.class).cadastrarDocumento(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Documento>> cadastrarDocumento(@RequestBody Documento documento) {
        Documento documentoSalvo = repositorio.save(documento);
        
       
        EntityModel<Documento> documentoModel = EntityModel.of(documentoSalvo);
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(documentoSalvo.getId())).withSelfRel());
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(documentoSalvo.getId(), null)).withRel("update"));
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(documentoSalvo.getId())).withRel("delete"));
        documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
        
        return new ResponseEntity<>(documentoModel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Documento>> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
        if (repositorio.existsById(id)) {
            Documento documento = repositorio.getById(id);
            DocumentoAtualizador atualizador = new DocumentoAtualizador();
            atualizador.atualizar(documento, atualizacao);
            Documento documentoAtualizado = repositorio.save(documento);
            
          
            EntityModel<Documento> documentoModel = EntityModel.of(documentoAtualizado);
            documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
            documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(id)).withRel("delete"));
            documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
            
            return new ResponseEntity<>(documentoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Documento documento = repositorio.getById(id);
            repositorio.delete(documento);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}