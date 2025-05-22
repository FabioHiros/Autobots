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
    public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, id);
        if (documento == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(documento, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Documento>> obterDocumentos() {
        List<Documento> documentos = repositorio.findAll();
        return new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Documento> cadastrarDocumento(@RequestBody Documento documento) {
        repositorio.save(documento);
        return new ResponseEntity<>(documento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
        if (repositorio.existsById(id)) {
            Documento documento = repositorio.getById(id);
            DocumentoAtualizador atualizador = new DocumentoAtualizador();
            atualizador.atualizar(documento, atualizacao);
            repositorio.save(documento);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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