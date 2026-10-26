package com.example.demo.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Prova;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Provas;
import com.example.demo.security.UsuarioPrincipal;
import com.example.demo.services.IntegracaoService;
import com.example.demo.services.PermissaoService;

/** Endpoints que combinam dados locais com dados da BrasilAPI (API externa). ADMIN e PROFESSOR. */
@RestController
@RequestMapping("/integracao")
public class IntegracaoController {

	private final IntegracaoService integracao;
	private final Provas provas;
	private final Alunos alunos;
	private final PermissaoService permissao;

	public IntegracaoController(IntegracaoService integracao, Provas provas, Alunos alunos,
			PermissaoService permissao) {
		this.integracao = integracao;
		this.provas = provas;
		this.alunos = alunos;
		this.permissao = permissao;
	}

	/** Prova (local) + feriados nacionais do ano (externo): avisa se cai em feriado/fim de semana. */
	@GetMapping("/provas/{id}/calendario")
	public Map<String, Object> calendario(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal usuario) {
		Prova prova = provas.findById(id)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Não existe Prova com esse id"));
		permissao.exigirVerProva(usuario, prova);
		return integracao.calendarioDaProva(prova);
	}

	/** Aluno (local) + endereço pelo CEP (externo). */
	@GetMapping("/alunos/{matricula}/localizacao")
	public Map<String, Object> localizacao(@PathVariable String matricula, @RequestParam String cep) {
		Aluno aluno = alunos.findByMatricula(matricula);
		if (aluno == null) {
			throw new RecursoNaoEncontradoException("Não existe Aluno com essa matricula");
		}
		return integracao.localizacaoDoAluno(aluno, cep);
	}
}
