package com.autobots.automanager.adaptadores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuario;

@SuppressWarnings("serial")
public class UserDetailsImpl implements UserDetails {

    private Cliente cliente;
    private boolean isBarcodeAuth = false;

    public UserDetailsImpl(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public UserDetailsImpl(Cliente cliente, boolean isBarcodeAuth) {
        this.cliente = cliente;
        this.isBarcodeAuth = isBarcodeAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
       
        if (cliente != null && cliente.getPerfil() != null) {
            String role = "ROLE_" + cliente.getPerfil().name();
            authorities.add(new SimpleGrantedAuthority(role));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        if (cliente == null) return null;
        
        if (isBarcodeAuth) {
  
            CredencialCodigoBarra credBarcode = cliente.getCredencialBarcode();
            return credBarcode != null ? credBarcode.getCodigo() : null;
        } else {
           
            CredencialUsuario credUsuario = cliente.getCredencialUsuario();
            return credUsuario != null ? credUsuario.getSenha() : null;
        }
    }

    @Override
    public String getUsername() {
        if (cliente == null) return null;
        
        if (isBarcodeAuth) {
          
            CredencialCodigoBarra credBarcode = cliente.getCredencialBarcode();
            return credBarcode != null ? "BARCODE:" + credBarcode.getCodigo() : null;
        } else {
           
            CredencialUsuario credUsuario = cliente.getCredencialUsuario();
            return credUsuario != null ? credUsuario.getNomeUsuario() : null;
        }
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (cliente == null) return false;
        
        if (isBarcodeAuth) {
            CredencialCodigoBarra credBarcode = cliente.getCredencialBarcode();
            return credBarcode != null && !credBarcode.isInativo();
        } else {
            CredencialUsuario credUsuario = cliente.getCredencialUsuario();
            return credUsuario != null && !credUsuario.isInativo();
        }
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public boolean isBarcodeAuth() {
        return isBarcodeAuth;
    }
}