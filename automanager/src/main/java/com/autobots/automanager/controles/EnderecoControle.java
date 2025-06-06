package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
        List<Endereco> enderecos = repositorio.findAll();
        Endereco endereco = selecionador.selecionar(enderecos, id);
        if (endereco == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(endereco, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Endereco>> obterEnderecos() {
        List<Endereco> enderecos = repositorio.findAll();
        return new ResponseEntity<>(enderecos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Endereco> cadastrarEndereco(@RequestBody Endereco endereco) {
        repositorio.save(endereco);
        return new ResponseEntity<>(endereco, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
        if (repositorio.existsById(id)) {
            Endereco endereco = repositorio.findById(id).get();
            EnderecoAtualizador atualizador = new EnderecoAtualizador();
            atualizador.atualizar(endereco, atualizacao);
            repositorio.save(endereco);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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