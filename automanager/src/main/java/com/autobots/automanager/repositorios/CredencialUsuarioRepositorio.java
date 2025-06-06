package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.autobots.automanager.entidades.CredencialUsuario;

public interface CredencialUsuarioRepositorio extends JpaRepository<CredencialUsuario, Long> {
}