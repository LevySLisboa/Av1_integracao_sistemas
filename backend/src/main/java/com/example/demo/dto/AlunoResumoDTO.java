package com.example.demo.dto;

import com.example.demo.models.entity.Aluno;

/** Visão reduzida do aluno (sem CPF, telefone e data de nascimento). */
public record AlunoResumoDTO(Long id, String nome, String email, String matricula, String curso,
		Integer semestreDeReferencia, Double pontuacaoMediaGeral) {

	public static AlunoResumoDTO de(Aluno a) {
		return new AlunoResumoDTO(a.getId(), a.getNome(), a.getEmail(), a.getMatricula(), a.getCurso(),
				a.getSemestreDeReferencia(), a.getPontuacaoMediaGeral());
	}
}
