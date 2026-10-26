package com.example.demo.dto;

import com.example.demo.models.entity.Papel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
		@NotBlank @Size(max = 150) String nome,
		@NotBlank @Email @Size(max = 150) String email,
		@NotBlank @Size(min = 10, max = 72, message = "A senha deve ter entre 10 e 72 caracteres") String senha,
		@NotNull Papel papel,
		String matricula) {
}
