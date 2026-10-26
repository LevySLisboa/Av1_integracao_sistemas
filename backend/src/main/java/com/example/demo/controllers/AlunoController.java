package com.example.demo.controllers;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AlunoResumoDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Papel;
import com.example.demo.models.repository.Alunos;
import com.example.demo.security.UsuarioPrincipal;

import jakarta.validation.Valid;

/**
 * Permissões (além das regras de URL em SecurityConfig):
 *  - ADMIN: CRUD completo, vê todos os dados.
 *  - PROFESSOR: lista/consulta alunos em visão reduzida (sem CPF/telefone/nascimento).
 *  - ALUNO: consulta SOMENTE o próprio cadastro (403 nos demais).
 */
@RestController
@RequestMapping("/alunos")
public class AlunoController {

	private final Alunos alunos;

	public AlunoController(Alunos alunos) {
		this.alunos = alunos;
	}

	@GetMapping({ "", "/" })
	public ResponseEntity<?> listar(@AuthenticationPrincipal UsuarioPrincipal usuario) {
		List<Aluno> todos = alunos.findAll();
		if (usuario.papel() == Papel.ADMIN) {
			return ResponseEntity.ok(todos);
		}
		return ResponseEntity.ok(todos.stream().map(AlunoResumoDTO::de).toList());
	}

	@GetMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<?> buscarPorMatricula(@PathVariable String matricula,
			@AuthenticationPrincipal UsuarioPrincipal usuario) {

		if (usuario.papel() == Papel.ALUNO && !matricula.equals(usuario.matricula())) {
			throw new AccessDeniedException("Um aluno só pode consultar o próprio cadastro.");
		}

		Aluno aluno = alunos.findByMatricula(matricula);
		if (aluno == null) {
			throw new RecursoNaoEncontradoException("Não existe Aluno com essa matricula");
		}

		if (usuario.papel() == Papel.PROFESSOR) {
			return ResponseEntity.ok(AlunoResumoDTO.de(aluno));
		}
		return ResponseEntity.ok(aluno);
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Aluno> criarAluno(@Valid @RequestBody Aluno aluno) {
		aluno.setId(null); // evita sobrescrever registro existente via corpo
		aluno.setAtivo(true);
		aluno.setMatricula(null);

		Aluno novoAluno = alunos.save(aluno);

		String ano = new SimpleDateFormat("yyyy").format(novoAluno.getDataCriacao());
		novoAluno.setMatricula(novoAluno.getId() + "" + ano);
		alunos.save(novoAluno);

		return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
	}

	@PutMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Aluno> editarAluno(@PathVariable String matricula, @Valid @RequestBody Aluno aluno) {
		Aluno alunoExistente = alunos.findByMatricula(matricula);
		if (alunoExistente == null) {
			throw new RecursoNaoEncontradoException("Não existe Aluno com essa matricula");
		}

		alunoExistente.setNome(aluno.getNome());
		alunoExistente.setEmail(aluno.getEmail());
		alunoExistente.setDataNascimento(aluno.getDataNascimento());
		alunoExistente.setCurso(aluno.getCurso());
		alunoExistente.setCpfCnpj(aluno.getCpfCnpj());
		alunoExistente.setSemestreDeReferencia(aluno.getSemestreDeReferencia());
		alunoExistente.setTelefone(aluno.getTelefone());
		alunoExistente.setPontuacaoMediaGeral(aluno.getPontuacaoMediaGeral());

		return ResponseEntity.ok(alunos.save(alunoExistente));
	}

	@DeleteMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Void> deletarAluno(@PathVariable String matricula) {
		Aluno aluno = alunos.findByMatricula(matricula);
		if (aluno == null) {
			throw new RecursoNaoEncontradoException("Não existe Aluno com essa matricula");
		}
		alunos.delete(aluno);
		return ResponseEntity.noContent().build();
	}
}
