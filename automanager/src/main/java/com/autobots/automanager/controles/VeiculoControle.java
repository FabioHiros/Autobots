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

import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.modelo.VeiculoSelecionador;
import com.autobots.automanager.repositorios.VeiculoRepositorio;

@RestController
@RequestMapping("/veiculo")
public class VeiculoControle {
    @Autowired
    private VeiculoRepositorio repositorio;
    @Autowired
    private VeiculoSelecionador selecionador;

    // ver veiculo espeficifico
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Veiculo>> obterVeiculo(@PathVariable long id) {
        List<Veiculo> veiculos = repositorio.findAll();
        Veiculo veiculo = selecionador.selecionar(veiculos, id);
        
        if (veiculo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Veiculo> veiculoModel = EntityModel.of(veiculo);
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(id)).withSelfRel());
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).atualizarVeiculo(id, new Veiculo())).withRel("update"));
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).excluirVeiculo(id)).withRel("delete"));
        veiculoModel.add(linkTo(VeiculoControle.class).withRel("veiculos"));
        
        if (veiculo.getProprietario() != null) {
            veiculoModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(veiculo.getProprietario().getId())).withRel("proprietario"));
        }
        
        return new ResponseEntity<>(veiculoModel, HttpStatus.OK);
    }

    // ver veiculos
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Veiculo>>> obterVeiculos() {
        List<Veiculo> veiculos = repositorio.findAll();
        
        List<EntityModel<Veiculo>> veiculoModels = veiculos.stream()
            .map(veiculo -> {
                EntityModel<Veiculo> veiculoModel = EntityModel.of(veiculo);
                veiculoModel.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(veiculo.getId())).withSelfRel());
                veiculoModel.add(linkTo(methodOn(VeiculoControle.class).atualizarVeiculo(veiculo.getId(), new Veiculo())).withRel("update"));
                veiculoModel.add(linkTo(methodOn(VeiculoControle.class).excluirVeiculo(veiculo.getId())).withRel("delete"));
                return veiculoModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Veiculo>> collectionModel = CollectionModel.of(veiculoModels);
        collectionModel.add(linkTo(VeiculoControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(VeiculoControle.class).cadastrarVeiculo(new Veiculo())).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // somento admin e gerente podem adicionar um veiculo
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PostMapping
    public ResponseEntity<EntityModel<Veiculo>> cadastrarVeiculo(@RequestBody Veiculo veiculo) {
        Veiculo veiculoSalvo = repositorio.save(veiculo);
        
        EntityModel<Veiculo> veiculoModel = EntityModel.of(veiculoSalvo);
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(veiculoSalvo.getId())).withSelfRel());
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).atualizarVeiculo(veiculoSalvo.getId(), new Veiculo())).withRel("update"));
        veiculoModel.add(linkTo(methodOn(VeiculoControle.class).excluirVeiculo(veiculoSalvo.getId())).withRel("delete"));
        veiculoModel.add(linkTo(VeiculoControle.class).withRel("veiculos"));
        
        return new ResponseEntity<>(veiculoModel, HttpStatus.CREATED);
    }

    // só admins e gerentes podem atualizar um veiculo
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Veiculo>> atualizarVeiculo(@PathVariable long id, @RequestBody Veiculo atualizacao) {
        if (repositorio.existsById(id)) {
            Veiculo veiculo = repositorio.getById(id);
            VeiculoAtualizador atualizador = new VeiculoAtualizador();
            atualizador.atualizar(veiculo, atualizacao);
            Veiculo veiculoAtualizado = repositorio.save(veiculo);
            
            EntityModel<Veiculo> veiculoModel = EntityModel.of(veiculoAtualizado);
            veiculoModel.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(id)).withSelfRel());
            veiculoModel.add(linkTo(methodOn(VeiculoControle.class).excluirVeiculo(id)).withRel("delete"));
            veiculoModel.add(linkTo(VeiculoControle.class).withRel("veiculos"));
            
            return new ResponseEntity<>(veiculoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // só gerente e admin podem deletas veiculos
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirVeiculo(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Veiculo veiculo = repositorio.getById(id);
            repositorio.delete(veiculo);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}