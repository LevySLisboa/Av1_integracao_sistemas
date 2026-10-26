package com.example.demo.models.entity;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.demo.models.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "prova")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE prova SET ativo = false WHERE id = ?")
public class Prova extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@NotBlank
	@Column(nullable = false)
	private String disciplina;

	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private Double nota;

	@NotNull
	@Min(1)
	@Max(3)
	@Column(nullable = false)
	private Integer av;

	@NotBlank
	@Column(nullable = false)
	private String assunto;

	// Dados sensíveis do professor/aluno nunca saem aninhados na prova
	@NotNull
	@JsonIgnoreProperties(value = { "salario", "cpfCnpj", "telefone", "dataNascimento", "dataAdmissao",
			"cargaHoraria" }, allowSetters = true)
	@ManyToOne
	@JoinColumn(name = "professor_id", nullable = false)
	private Professor professor;

	@NotBlank
	@Column(nullable = false)
	private String turma;

	@NotNull
	@JsonIgnoreProperties(value = { "cpfCnpj", "telefone", "dataNascimento", "pontuacaoMediaGeral" }, allowSetters = true)
	@ManyToOne
	@JoinColumn(name = "aluno_id", nullable = false)
	private Aluno aluno;

	/** Data em que a prova será aplicada (usada na integração com feriados). */
	@Column(name = "data_aplicacao")
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataAplicacao;

	public String getDisciplina() { return disciplina; }
	public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

	public Double getNota() { return nota; }
	public void setNota(Double nota) { this.nota = nota; }

	public Integer getAv() { return av; }
	public void setAv(Integer av) { this.av = av; }

	public String getAssunto() { return assunto; }
	public void setAssunto(String assunto) { this.assunto = assunto; }

	public Professor getProfessor() { return professor; }
	public void setProfessor(Professor professor) { this.professor = professor; }

	public String getTurma() { return turma; }
	public void setTurma(String turma) { this.turma = turma; }

	public Aluno getAluno() { return aluno; }
	public void setAluno(Aluno aluno) { this.aluno = aluno; }

	public LocalDate getDataAplicacao() { return dataAplicacao; }
	public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }
}
