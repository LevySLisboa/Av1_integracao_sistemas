package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UsuarioRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.exception.ConflitoException;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RequisicaoInvalidaException;
import com.example.demo.models.entity.Papel;
import com.example.demo.models.entity.Usuario;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Professores;
import com.example.demo.models.repository.Usuarios;
import com.example.demo.security.UsuarioPrincipal;

import jakarta.validation.Valid;

/** Gestão de contas de login. Restrito ao perfil ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final Usuarios usuarios;
	private final Alunos alunos;
	private final Professores professores;
	private final PasswordEncoder encoder;

	public UsuarioController(Usuarios usuarios, Alunos alunos, Professores professores, PasswordEncoder encoder) {
		this.usuarios = usuarios;
		this.alunos = alunos;
		this.professores = professores;
		this.encoder = encoder;
	}

	@GetMapping({ "", "/" })
	public List<UsuarioResponse> listar() {
		return usuarios.findAll().stream().map(UsuarioResponse::de).toList();
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest req) {
		String email = req.email().trim().toLowerCase();
		if (usuarios.existsByEmail(email)) {
			throw new ConflitoException("Já existe um usuário com este email.");
		}

		String matricula = null;
		if (req.papel() == Papel.ALUNO) {
			matricula = exigirMatricula(req.matricula());
			if (alunos.findByMatricula(matricula) == null) {
				throw new RequisicaoInvalidaException("Não existe aluno com esta matrícula.");
			}
		} else if (req.papel() == Papel.PROFESSOR) {
			matricula = exigirMatricula(req.matricula());
			if (professores.findByMatricula(matricula) == null) {
				throw new RequisicaoInvalidaException("Não existe professor com esta matrícula.");
			}
		}

		Usuario u = new Usuario();
		u.setNome(req.nome().trim());
		u.setEmail(email);
		u.setSenhaHash(encoder.encode(req.senha()));
		u.setPapel(req.papel());
		u.setMatricula(matricula);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(usuarios.save(u)));
	}

	@DeleteMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Void> remover(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal logado) {
		if (logado.id().equals(id)) {
			throw new RequisicaoInvalidaException("Você não pode remover a própria conta.");
		}
		Usuario u = usuarios.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Não existe usuário com esse id"));
		usuarios.delete(u); // exclusão lógica (@SQLDelete)
		return ResponseEntity.noContent().build();
	}

	private String exigirMatricula(String matricula) {
		if (matricula == null || matricula.isBlank()) {
			throw new RequisicaoInvalidaException("Informe a matrícula do aluno/professor vinculado.");
		}
		return matricula.trim();
	}
}
