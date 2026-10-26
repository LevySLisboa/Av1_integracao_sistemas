package com.example.demo.security;

import java.io.Serializable;

import com.example.demo.models.entity.Papel;

/** Identidade do usuário autenticado, guardada na sessão. Não contém segredos. */
public record UsuarioPrincipal(Long id, String email, String nome, Papel papel, String matricula)
		implements Serializable {
}
