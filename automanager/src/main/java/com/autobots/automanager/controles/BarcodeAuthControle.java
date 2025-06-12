package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.servicos.CodigoBarrasGerador;

@RestController
@RequestMapping("/barcode")
public class BarcodeAuthControle {

    @Autowired
    private ClienteRepositorio clienteRepositorio;
    
    @Autowired
    private ClienteSelecionador clienteSelecionador;
    
    @Autowired
    private CodigoBarrasGerador codigoBarrasGerador;

    // qualquer usuario autenticado pode ver a string do seu codigo de barras
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/meu-codigo")
    public ResponseEntity<?> obterMeuCodigoBarra() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Cliente cliente = userDetails.getCliente();
            
            if (cliente.getCredencialBarcode() != null) {
                return ResponseEntity.ok().body("{\"codigo\":\"" + 
                    cliente.getCredencialBarcode().getCodigo() + "\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    // só admin  podem ver o codigo dos outros
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/cliente/{id}")
    public ResponseEntity<?> obterCodigoBarraCliente(@PathVariable long id) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        Cliente cliente = clienteSelecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        if (cliente.getCredencialBarcode() != null) {
            return ResponseEntity.ok().body("{\"clienteId\":" + id + 
                ",\"codigo\":\"" + cliente.getCredencialBarcode().getCodigo() + "\"}");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // só admin  podem gerar novamente o codigo dos outros
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/regenerar/{id}")
    public ResponseEntity<?> regenerarCodigoBarra(@PathVariable long id) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        Cliente cliente = clienteSelecionador.selecionar(clientes, id);
        
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        String novoCodigo = codigoBarrasGerador.gerarCodigoPorPerfil(
            cliente.getPerfil() != null ? cliente.getPerfil().toString() : "CLIENTE"
        );
        
        if (cliente.getCredencialBarcode() != null) {
            cliente.getCredencialBarcode().setCodigo(novoCodigo);
        } else {
            CredencialCodigoBarra novaCredencial = new CredencialCodigoBarra();
            novaCredencial.setCodigo(novoCodigo);
            novaCredencial.setCriacao(new java.util.Date());
            novaCredencial.setInativo(false);
            cliente.addCredencial(novaCredencial);
        }
        
        clienteRepositorio.save(cliente);
        
        return ResponseEntity.ok().body("{\"message\":\"Código de barras regenerado com sucesso\"," +
            "\"novoCodigo\":\"" + novoCodigo + "\"}");
    }

    // só admin  podem gerar novamente seus códigos
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/regenerar-meu")
    public ResponseEntity<?> regenerarMeuCodigoBarra() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Cliente cliente = userDetails.getCliente();
            
            String novoCodigo = codigoBarrasGerador.gerarCodigoPorPerfil(
                cliente.getPerfil() != null ? cliente.getPerfil().toString() : "CLIENTE"
            );
            
            if (cliente.getCredencialBarcode() != null) {
                cliente.getCredencialBarcode().setCodigo(novoCodigo);
                clienteRepositorio.save(cliente);
                
                return ResponseEntity.ok().body("{\"message\":\"Seu código de barras foi regenerado\"," +
                    "\"novoCodigo\":\"" + novoCodigo + "\"}");
            } else {
                return ResponseEntity.badRequest().body("{\"error\":\"Credencial de código de barras não encontrada\"}");
            }
        }
        return ResponseEntity.badRequest().build();
    }

    // só admin pode validar código dos outros
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/validar/{codigo}")
    public ResponseEntity<?> validarCodigoBarra(@PathVariable String codigo) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        
        for (Cliente cliente : clientes) {
            if (cliente.getCredencialBarcode() != null && 
                cliente.getCredencialBarcode().getCodigo().equals(codigo)) {
                return ResponseEntity.ok().body("{\"valido\":true," +
                    "\"clienteId\":" + cliente.getId() + "," +
                    "\"clienteNome\":\"" + cliente.getNome() + "\"," +
                    "\"perfil\":\"" + cliente.getPerfil() + "\"}");
            }
        }
        
        return ResponseEntity.ok().body("{\"valido\":false}");
    }
}