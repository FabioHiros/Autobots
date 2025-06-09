package com.autobots.automanager.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.PerfilUsuario;
import com.autobots.automanager.entidades.Telefone;

import lombok.Data;

@Data
public class ClienteRegistroDTO {
	private String nome;
	private String nomeSocial;
	private Date dataNascimento;
	private PerfilUsuario perfil;
	private List<Documento> documentos = new ArrayList<>();
	private Endereco endereco;
	private List<Telefone> telefones = new ArrayList<>();
	
	
	private String nomeUsuario;
	private String senha;
}