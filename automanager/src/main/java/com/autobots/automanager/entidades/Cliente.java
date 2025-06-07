package com.autobots.automanager.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
public class Cliente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String nome;
	
	@Column
	private String nomeSocial;
	
	@Column
	private Date dataNascimento;
	
	@Column
	private Date dataCadastro;
	
	@Enumerated(EnumType.STRING)
	@Column
	private PerfilUsuario perfil;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "cliente_id")
	private List<Documento> documentos = new ArrayList<>();
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	

	@OneToMany(cascade = CascadeType.ALL, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "cliente_id")
	private List<Telefone> telefones = new ArrayList<>();
	
	@OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Veiculo> veiculos = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name = "empresa_id")
	@JsonIgnore
	private Empresa empresa;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private CredencialUsuario credencial;
	
	@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Venda> vendas = new ArrayList<>();
	
	
	public void addDocumento(Documento documento) {
		documentos.add(documento);
	}
	
	public void removeDocumento(Documento documento) {
		documentos.remove(documento);
	}
	
	public void addTelefone(Telefone telefone) {
		telefones.add(telefone);
	}
	
	public void removeTelefone(Telefone telefone) {
		telefones.remove(telefone);
	}
}