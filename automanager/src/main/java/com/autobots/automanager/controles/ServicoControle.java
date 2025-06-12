package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.modelo.ServicoSelecionador;
import com.autobots.automanager.repositorios.ServicoRepositorio;

@RestController
@RequestMapping("/servico")
public class ServicoControle {
    @Autowired
    private ServicoRepositorio repositorio;
    @Autowired
    private ServicoSelecionador selecionador;

    // ver servico especifico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Servico>> obterServico(@PathVariable long id) {
        List<Servico> servicos = repositorio.findAll();
        Servico servico = selecionador.selecionar(servicos, id);
        
        if (servico == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Servico> servicoModel = EntityModel.of(servico);
        servicoModel.add(linkTo(methodOn(ServicoControle.class).obterServico(id)).withSelfRel());
        servicoModel.add(linkTo(methodOn(ServicoControle.class).atualizarServico(id, null)).withRel("update"));
        servicoModel.add(linkTo(methodOn(ServicoControle.class).excluirServico(id)).withRel("delete"));
        servicoModel.add(linkTo(ServicoControle.class).withRel("servicos"));
        
        return new ResponseEntity<>(servicoModel, HttpStatus.OK);
    }

    // ver servico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Servico>>> obterServicos() {
        List<Servico> servicos = repositorio.findAll();
        
        List<EntityModel<Servico>> servicoModels = servicos.stream()
            .map(servico -> {
                EntityModel<Servico> servicoModel = EntityModel.of(servico);
                servicoModel.add(linkTo(methodOn(ServicoControle.class).obterServico(servico.getId())).withSelfRel());
                return servicoModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Servico>> collectionModel = CollectionModel.of(servicoModels);
        collectionModel.add(linkTo(ServicoControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // criar servico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PostMapping
    public ResponseEntity<EntityModel<Servico>> cadastrarServico(@RequestBody Servico servico) {
        Servico servicoSalvo = repositorio.save(servico);
        
        EntityModel<Servico> servicoModel = EntityModel.of(servicoSalvo);
        servicoModel.add(linkTo(methodOn(ServicoControle.class).obterServico(servicoSalvo.getId())).withSelfRel());
        
        return new ResponseEntity<>(servicoModel, HttpStatus.CREATED);
    }

    // atualizar servico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Servico>> atualizarServico(@PathVariable long id, @RequestBody Servico atualizacao) {
        if (repositorio.existsById(id)) {
            Servico servico = repositorio.getById(id);
            ServicoAtualizador atualizador = new ServicoAtualizador();
            atualizador.atualizar(servico, atualizacao);
            Servico servicoAtualizado = repositorio.save(servico);
            
            EntityModel<Servico> servicoModel = EntityModel.of(servicoAtualizado);
            servicoModel.add(linkTo(methodOn(ServicoControle.class).obterServico(id)).withSelfRel());
            
            return new ResponseEntity<>(servicoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //deletar servico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirServico(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Servico servico = repositorio.getById(id);
            repositorio.delete(servico);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}