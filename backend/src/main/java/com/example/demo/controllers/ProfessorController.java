package com.example.demo.controllers;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProfessorResumoDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.models.entity.Papel;
import com.example.demo.models.entity.Professor;
import com.example.demo.models.repository.Professores;
import com.example.demo.security.UsuarioPrincipal;

import jakarta.validation.Valid;

/**
 * Permissões:
 *  - ADMIN: CRUD completo, vê salário/CPF/etc.
 *  - PROFESSOR: vê o PRÓPRIO cadastro completo; os demais em visão reduzida.
 *  - ALUNO: apenas visão reduzida (nome, email, departamento...).
 */
@RestController
@RequestMapping("/professor")
public class ProfessorController {

	private final Professores professores;

	public ProfessorController(Professores professores) {
		this.professores = professores;
	}

	@GetMapping({ "", "/" })
	public ResponseEntity<?> listar(@AuthenticationPrincipal UsuarioPrincipal usuario) {
		List<Professor> todos = professores.findAll();
		if (usuario.papel() == Papel.ADMIN) {
			return ResponseEntity.ok(todos);
		}
		return ResponseEntity.ok(todos.stream().map(p -> visao(usuario, p)).toList());
	}

	@GetMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<?> buscarPorMatricula(@PathVariable String matricula,
			@AuthenticationPrincipal UsuarioPrincipal usuario) {
		Professor professor = professores.findByMatricula(matricula);
		if (professor == null) {
			throw new RecursoNaoEncontradoException("Não existe Professor com essa matricula");
		}
		return ResponseEntity.ok(usuario.papel() == Papel.ADMIN ? professor : visao(usuario, professor));
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Professor> criarProfessor(@Valid @RequestBody Professor professor) {
		professor.setId(null);
		professor.setAtivo(true);
		professor.setMatricula(null);

		Professor novoProfessor = professores.save(professor);

		String ano = new SimpleDateFormat("yyyy").format(novoProfessor.getDataCriacao());
		novoProfessor.setMatricula(novoProfessor.getId() + "" + ano);
		professores.save(novoProfessor);

		return ResponseEntity.status(HttpStatus.CREATED).body(novoProfessor);
	}

	@PutMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Professor> editarProfessor(@PathVariable String matricula,
			@Valid @RequestBody Professor professor) {
		Professor professorExistente = professores.findByMatricula(matricula);
		if (professorExistente == null) {
			throw new RecursoNaoEncontradoException("Não existe Professor com essa matricula");
		}

		professorExistente.setNome(professor.getNome());
		professorExistente.setEmail(professor.getEmail());
		professorExistente.setDataNascimento(professor.getDataNascimento());
		professorExistente.setCpfCnpj(professor.getCpfCnpj());
		professorExistente.setTelefone(professor.getTelefone());
		professorExistente.setDepartamento(professor.getDepartamento());
		professorExistente.setEspecializacao(professor.getEspecializacao());
		professorExistente.setTitulacao(professor.getTitulacao());
		professorExistente.setSalario(professor.getSalario());
		professorExistente.setCargaHoraria(professor.getCargaHoraria());
		professorExistente.setDataAdmissao(professor.getDataAdmissao());

		return ResponseEntity.ok(professores.save(professorExistente));
	}

	@DeleteMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Void> deletarProfessor(@PathVariable String matricula) {
		Professor professor = professores.findByMatricula(matricula);
		if (professor == null) {
			throw new RecursoNaoEncontradoException("Não existe Professor com essa matricula");
		}
		professores.delete(professor);
		return ResponseEntity.noContent().build();
	}

	/** O próprio professor enxerga tudo do seu cadastro; os outros perfis, só o resumo. */
	private Object visao(UsuarioPrincipal usuario, Professor p) {
		boolean proprio = usuario.papel() == Papel.PROFESSOR && p.getMatricula() != null
				&& p.getMatricula().equals(usuario.matricula());
		return proprio ? p : ProfessorResumoDTO.de(p);
	}
}
