package com.autobots.automanager.entidades;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class CredencialUsuario extends Credencial {
    @Column(unique = true)
    private String nomeUsuario;
    
    @Column
    private String senha;
    
    @Column
    private Date dataUltimoAcesso;
    
    @Column
    private Date dataCriacao;
}