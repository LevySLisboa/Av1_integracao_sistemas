package com.example.demo.dto;

import com.example.demo.models.entity.Papel;
import com.example.demo.models.entity.Usuario;

/** Nunca expõe senhaHash nem contadores internos de segurança. */
public record UsuarioResponse(Long id, String nome, String email, Papel papel, String matricula) {

	public static UsuarioResponse de(Usuario u) {
		return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPapel(), u.getMatricula());
	}
}
