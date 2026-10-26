package com.example.demo.dto;

import com.example.demo.models.entity.Professor;

/** Visão reduzida do professor (sem salário, CPF, telefone e datas pessoais). */
public record ProfessorResumoDTO(Long id, String nome, String email, String matricula, String departamento,
		String especializacao, String titulacao) {

	public static ProfessorResumoDTO de(Professor p) {
		return new ProfessorResumoDTO(p.getId(), p.getNome(), p.getEmail(), p.getMatricula(), p.getDepartamento(),
				p.getEspecializacao(), p.getTitulacao());
	}
}
