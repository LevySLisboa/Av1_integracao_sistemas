package com.example.demo.models.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.entity.Usuario;

public interface Usuarios extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);
}
