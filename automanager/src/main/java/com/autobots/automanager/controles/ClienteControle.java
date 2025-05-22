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
    public ResponseEntity<Cliente> obterCliente(@PathVariable long id) {
        List<Cliente> clientes = repositorio.findAll();
        Cliente cliente = selecionador.selecionar(clientes, id);
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> obterClientes() {
        List<Cliente> clientes = repositorio.findAll();
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Cliente> cadastrarCliente(@RequestBody Cliente cliente) {
        repositorio.save(cliente);
        return new ResponseEntity<>(cliente, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarCliente(@PathVariable long id, @RequestBody Cliente atualizacao) {
        if (repositorio.existsById(id)) {
            Cliente cliente = repositorio.getById(id);
            ClienteAtualizador atualizador = new ClienteAtualizador();
            atualizador.atualizar(cliente, atualizacao);
            repositorio.save(cliente);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
}