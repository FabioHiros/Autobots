package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class CredencialUsuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String nomeUsuario;
	
	@Column
	private String senha;
	
	@Column
	private boolean inativo;
	
	@Column
	private Date dataUltimoAcesso;
	
	@Column
	private Date dataCriacao;
}