package com.example.demo.controllers;

import java.util.List;
import java.util.Objects;

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

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RequisicaoInvalidaException;
import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Papel;
import com.example.demo.models.entity.Professor;
import com.example.demo.models.entity.Prova;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Professores;
import com.example.demo.models.repository.Provas;
import com.example.demo.security.UsuarioPrincipal;
import com.example.demo.services.PermissaoService;
import com.example.demo.services.PontuacaoMediaService;

import jakarta.validation.Valid;

/**
 * Permissões:
 *  - ADMIN: tudo.
 *  - PROFESSOR: lê, cria, edita e remove SOMENTE provas em que ele é o professor.
 *  - ALUNO: lê SOMENTE as próprias provas (não cria/edita/remove).
 */
@RestController
@RequestMapping("/provas")
public class ProvaController {

	private final Provas provas;
	private final Professores professores;
	private final Alunos alunos;
	private final PermissaoService permissao;
	private final PontuacaoMediaService pontuacaoMedia;

	public ProvaController(Provas provas, Professores professores, Alunos alunos, PermissaoService permissao,
			PontuacaoMediaService pontuacaoMedia) {
		this.provas = provas;
		this.professores = professores;
		this.alunos = alunos;
		this.permissao = permissao;
		this.pontuacaoMedia = pontuacaoMedia;
	}

	@GetMapping({ "", "/" })
	public ResponseEntity<List<Prova>> listar(@AuthenticationPrincipal UsuarioPrincipal usuario) {
		List<Prova> resultado = switch (usuario.papel()) {
		case ADMIN -> provas.findAll();
		case PROFESSOR -> usuario.matricula() == null ? List.of() : provas.findByProfessor_Matricula(usuario.matricula());
		case ALUNO -> usuario.matricula() == null ? List.of() : provas.findByAluno_Matricula(usuario.matricula());
		};
		return ResponseEntity.ok(resultado);
	}

	@GetMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Prova> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal usuario) {
		Prova prova = buscar(id);
		permissao.exigirVerProva(usuario, prova);
		return ResponseEntity.ok(prova);
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Prova> criarProva(@Valid @RequestBody Prova prova,
			@AuthenticationPrincipal UsuarioPrincipal usuario) {
		prova.setId(null);
		prova.setAtivo(true);
		resolverReferencias(prova, usuario);
		Prova salva = provas.save(prova);
		pontuacaoMedia.recalcular(salva.getAluno());
		return ResponseEntity.status(HttpStatus.CREATED).body(salva);
	}

	@PutMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Prova> editarProva(@PathVariable Long id, @Valid @RequestBody Prova prova,
			@AuthenticationPrincipal UsuarioPrincipal usuario) {
		Prova provaExistente = buscar(id);
		permissao.exigirVerProva(usuario, provaExistente);
		Aluno alunoAnterior = provaExistente.getAluno();
		resolverReferencias(prova, usuario);

		provaExistente.setDisciplina(prova.getDisciplina());
		provaExistente.setNota(prova.getNota());
		provaExistente.setAv(prova.getAv());
		provaExistente.setAssunto(prova.getAssunto());
		provaExistente.setProfessor(prova.getProfessor());
		provaExistente.setTurma(prova.getTurma());
		provaExistente.setAluno(prova.getAluno());
		provaExistente.setDataAplicacao(prova.getDataAplicacao());

		Prova salva = provas.save(provaExistente);
		pontuacaoMedia.recalcular(alunoAnterior);
		if (!alunoAnterior.getId().equals(salva.getAluno().getId())) {
			pontuacaoMedia.recalcular(salva.getAluno());
		}
		return ResponseEntity.ok(salva);
	}

	@DeleteMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Void> deletarProva(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal usuario) {
		Prova prova = buscar(id);
		permissao.exigirVerProva(usuario, prova);
		provas.delete(prova);
		pontuacaoMedia.recalcular(prova.getAluno());
		return ResponseEntity.noContent().build();
	}

	private Prova buscar(Long id) {
		return provas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Não existe Prova com esse id"));
	}

	/** Troca os {"id":N} do corpo pelas entidades reais e aplica a regra de posse do professor. */
	private void resolverReferencias(Prova prova, UsuarioPrincipal usuario) {
		if (prova.getProfessor() == null || prova.getProfessor().getId() == null || prova.getAluno() == null
				|| prova.getAluno().getId() == null) {
			throw new RequisicaoInvalidaException("Informe professor.id e aluno.id.");
		}
		Professor professor = professores.findById(prova.getProfessor().getId())
				.orElseThrow(() -> new RequisicaoInvalidaException("O professor informado não existe."));
		Aluno aluno = alunos.findById(prova.getAluno().getId())
				.orElseThrow(() -> new RequisicaoInvalidaException("O aluno informado não existe."));

		if (usuario.papel() == Papel.PROFESSOR
				&& (usuario.matricula() == null || !Objects.equals(professor.getMatricula(), usuario.matricula()))) {
			throw new AccessDeniedException("Um professor só pode gerenciar provas atribuídas a ele.");
		}
		prova.setProfessor(professor);
		prova.setAluno(aluno);
	}
}
