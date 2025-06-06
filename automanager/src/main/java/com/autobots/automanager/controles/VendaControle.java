package com.autobots.automanager.controles;

import java.util.Date;
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

import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.VendaAtualizador;
import com.autobots.automanager.modelo.VendaSelecionador;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/venda")
public class VendaControle {
    @Autowired
    private VendaRepositorio repositorio;
    @Autowired
    private VendaSelecionador selecionador;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venda>> obterVenda(@PathVariable long id) {
        List<Venda> vendas = repositorio.findAll();
        Venda venda = selecionador.selecionar(vendas, id);
        
        if (venda == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Venda> vendaModel = EntityModel.of(venda);
        
        // Self link
        vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
        
        // CRUD links
        vendaModel.add(linkTo(methodOn(VendaControle.class).atualizarVenda(id, null)).withRel("update"));
        vendaModel.add(linkTo(methodOn(VendaControle.class).excluirVenda(id)).withRel("delete"));
        
        // Collection link
        vendaModel.add(linkTo(VendaControle.class).withRel("vendas"));
        
        // Related entities links
        if (venda.getCliente() != null) {
            vendaModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(venda.getCliente().getId())).withRel("cliente"));
        }
        
        if (venda.getFuncionario() != null) {
            vendaModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(venda.getFuncionario().getId())).withRel("funcionario"));
        }
        
        if (venda.getMercadoria() != null) {
            vendaModel.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(venda.getMercadoria().getId())).withRel("mercadoria"));
        }
        
        if (venda.getServico() != null) {
            vendaModel.add(linkTo(methodOn(ServicoControle.class).obterServico(venda.getServico().getId())).withRel("servico"));
        }
        
        if (venda.getVeiculo() != null) {
            vendaModel.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(venda.getVeiculo().getId())).withRel("veiculo"));
        }
        
        if (venda.getEmpresa() != null) {
            vendaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(venda.getEmpresa().getId())).withRel("empresa"));
        }
        
        return new ResponseEntity<>(vendaModel, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Venda>>> obterVendas() {
        List<Venda> vendas = repositorio.findAll();
        
        List<EntityModel<Venda>> vendaModels = vendas.stream()
            .map(venda -> {
                EntityModel<Venda> vendaModel = EntityModel.of(venda);
                vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(venda.getId())).withSelfRel());
                vendaModel.add(linkTo(methodOn(VendaControle.class).atualizarVenda(venda.getId(), null)).withRel("update"));
                vendaModel.add(linkTo(methodOn(VendaControle.class).excluirVenda(venda.getId())).withRel("delete"));
                return vendaModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Venda>> collectionModel = CollectionModel.of(vendaModels);
        collectionModel.add(linkTo(VendaControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(VendaControle.class).cadastrarVenda(null)).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EntityModel<Venda>> cadastrarVenda(@RequestBody Venda venda) {
        // Set the creation date automatically
        if (venda.getCadastro() == null) {
            venda.setCadastro(new java.util.Date());
        }
        
        Venda vendaSalva = repositorio.save(venda);
        
        EntityModel<Venda> vendaModel = EntityModel.of(vendaSalva);
        vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(vendaSalva.getId())).withSelfRel());
        vendaModel.add(linkTo(methodOn(VendaControle.class).atualizarVenda(vendaSalva.getId(), null)).withRel("update"));
        vendaModel.add(linkTo(methodOn(VendaControle.class).excluirVenda(vendaSalva.getId())).withRel("delete"));
        vendaModel.add(linkTo(VendaControle.class).withRel("vendas"));
        
        return new ResponseEntity<>(vendaModel, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<EntityModel<Venda>> atualizarVenda(@PathVariable long id, @RequestBody Venda atualizacao) {
        if (repositorio.existsById(id)) {
            Venda venda = repositorio.getById(id);
            VendaAtualizador atualizador = new VendaAtualizador();
            atualizador.atualizar(venda, atualizacao);
            Venda vendaAtualizada = repositorio.save(venda);
            
            EntityModel<Venda> vendaModel = EntityModel.of(vendaAtualizada);
            vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
            vendaModel.add(linkTo(methodOn(VendaControle.class).excluirVenda(id)).withRel("delete"));
            vendaModel.add(linkTo(VendaControle.class).withRel("vendas"));
            
            return new ResponseEntity<>(vendaModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Venda venda = repositorio.getById(id);
            repositorio.delete(venda);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}