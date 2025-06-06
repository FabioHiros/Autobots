package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
public class Empresa {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String razaoSocial;
	
	@Column
	private String nomeFantasia;
	
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private List<Telefone> telefones = new ArrayList<>();
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	
	@Column
	private Date dataCadastro;
	
	@OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Cliente> usuarios = new ArrayList<>();
	
	@OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Mercadoria> mercadorias = new ArrayList<>();
	
	@OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Servico> servicos = new ArrayList<>();
	
	@OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Venda> vendas = new ArrayList<>();
}