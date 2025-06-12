package com.autobots.automanager.adaptadores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuario;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    private Cliente obterPorNomeUsuario(String nomeUsuario) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        
        for (Cliente cliente : clientes) {
            CredencialUsuario credUsuario = cliente.getCredencialUsuario();
            if (credUsuario != null && 
                credUsuario.getNomeUsuario() != null && 
                credUsuario.getNomeUsuario().equals(nomeUsuario)) {
                return cliente;
            }
        }
        return null;
    }
    
    private Cliente obterPorCodigoBarra(String codigo) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        
        for (Cliente cliente : clientes) {
            CredencialCodigoBarra credBarcode = cliente.getCredencialBarcode();
            if (credBarcode != null && 
                credBarcode.getCodigo() != null && 
                credBarcode.getCodigo().equals(codigo)) {
                return cliente;
            }
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Cliente cliente = null;
            
            // Check if it's a barcode authentication
            if (username.startsWith("BARCODE:")) {
                String codigo = username.substring(8); // Remove "BARCODE:" prefix
                cliente = this.obterPorCodigoBarra(codigo);
                
                if (cliente != null) {
                    return new UserDetailsImpl(cliente, true); // Mark as barcode auth
                }
            } else {
                // Regular username authentication
                cliente = this.obterPorNomeUsuario(username);
                
                if (cliente != null) {
                    return new UserDetailsImpl(cliente, false); // Mark as username auth
                }
            }
            
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error in loadUserByUsername: " + e.getMessage());
            e.printStackTrace();
            throw new UsernameNotFoundException("Erro ao carregar usuário: " + username, e);
        }
    }
}