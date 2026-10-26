package com.example.demo.services;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.example.demo.models.entity.Prova;
import com.example.demo.security.UsuarioPrincipal;

/** Regras de posse: o que cada perfil pode ver dentro de uma prova. */
@Component
public class PermissaoService {

	public boolean podeVerProva(UsuarioPrincipal u, Prova p) {
		if (u.papel() == null) {
			return false;
		}
		return switch (u.papel()) {
		case ADMIN -> true;
		case PROFESSOR -> u.matricula() != null && p.getProfessor() != null
				&& Objects.equals(p.getProfessor().getMatricula(), u.matricula());
		case ALUNO -> u.matricula() != null && p.getAluno() != null
				&& Objects.equals(p.getAluno().getMatricula(), u.matricula());
		};
	}

	public void exigirVerProva(UsuarioPrincipal u, Prova p) {
		if (!podeVerProva(u, p)) {
			throw new AccessDeniedException("Você não tem permissão para acessar esta prova.");
		}
	}
}
