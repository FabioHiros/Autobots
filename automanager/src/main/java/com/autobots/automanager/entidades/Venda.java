package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Entity
public class Venda {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private Date cadastro;
	
	@Column
	private String identificacao;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cliente_id")
	@JsonIgnoreProperties({"vendas", "empresa", "veiculos", "credenciais", "documentos", "telefones"})
	private Cliente cliente;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "funcionario_id")
	@JsonIgnoreProperties({"vendas", "empresa", "veiculos", "credenciais", "documentos", "telefones"})
	private Cliente funcionario;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mercadoria_id")
	@JsonIgnoreProperties({"vendas", "empresa"})
	private Mercadoria mercadoria;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "servico_id")
	@JsonIgnoreProperties({"vendas", "empresa"})
	private Servico servico;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "veiculo_id")
	@JsonIgnoreProperties({"vendas", "proprietario"})
	private Veiculo veiculo;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "empresa_id")
	@JsonIgnoreProperties({"vendas", "usuarios", "mercadorias", "servicos", "telefones"})
	private Empresa empresa;
}