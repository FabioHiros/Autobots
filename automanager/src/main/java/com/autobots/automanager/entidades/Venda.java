package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	
	@ManyToOne
	@JoinColumn(name = "cliente_id")
	@JsonIgnoreProperties({"vendas", "empresa", "veiculos"})
	private Cliente cliente;
	
	@ManyToOne
	@JoinColumn(name = "funcionario_id")
	@JsonIgnoreProperties({"vendas", "empresa", "veiculos"})
	private Cliente funcionario;
	
	@ManyToOne
	@JoinColumn(name = "mercadoria_id")
	@JsonIgnoreProperties({"vendas", "empresa"})
	private Mercadoria mercadoria;
	
	@ManyToOne
	@JoinColumn(name = "servico_id")
	@JsonIgnoreProperties({"vendas", "empresa"})
	private Servico servico;
	
	@ManyToOne
	@JoinColumn(name = "veiculo_id")
	@JsonIgnoreProperties({"vendas", "proprietario"})
	private Veiculo veiculo;
	
	@ManyToOne
	@JoinColumn(name = "empresa_id")
	@JsonIgnoreProperties({"vendas", "usuarios", "mercadorias", "servicos"})
	private Empresa empresa;
}