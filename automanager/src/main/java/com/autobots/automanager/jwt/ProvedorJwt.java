package com.autobots.automanager.jwt;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;

@Data
@Component
public class ProvedorJwt {

    @Value("${jwt.secret:autobots-secret-key-very-secure}")
    private String assinatura;

    @Value("${jwt.expiration:86400000}") 
    private Long duracao;

    private AnalisadorJwt analisador;
    private ValidadorJwt validador;

  
    public String proverJwt(String nomeUsuario) {
        Date expiracao = new Date(System.currentTimeMillis() + duracao);
        
        String jwt = Jwts.builder()
                .setSubject(nomeUsuario)
                .setExpiration(expiracao)
                .signWith(SignatureAlgorithm.HS512, assinatura.getBytes())
                .compact();
        return jwt;
    }

    
    public String proverJwtComAutoridades(String nomeUsuario, Collection<? extends GrantedAuthority> authorities) {
        Date expiracao = new Date(System.currentTimeMillis() + duracao);
        
     
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        String jwt = Jwts.builder()
                .setSubject(nomeUsuario)
                .claim("authorities", roles)
                .setExpiration(expiracao)
                .signWith(SignatureAlgorithm.HS512, assinatura.getBytes())
                .compact();
        return jwt;
    }

    public boolean validarJwt(String jwt) {
        analisador = new AnalisadorJwt(assinatura, jwt);
        validador = new ValidadorJwt();
        return validador.validar(analisador.obterReivindicacoes());
    }

    public String obterNomeUsuario(String jwt) {
        analisador = new AnalisadorJwt(assinatura, jwt);
        Claims reivindicacoes = analisador.obterReivindicacoes();
        return analisador.obterNomeUsuario(reivindicacoes);
    }
    
  
    public String obterAutoridades(String jwt) {
        analisador = new AnalisadorJwt(assinatura, jwt);
        Claims reivindicacoes = analisador.obterReivindicacoes();
        return reivindicacoes != null ? (String) reivindicacoes.get("authorities") : null;
    }
}