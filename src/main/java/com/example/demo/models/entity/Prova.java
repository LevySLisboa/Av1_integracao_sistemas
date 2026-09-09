package com.example.demo.models.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.demo.models.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prova")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE prova SET ativo = false WHERE id = ?")
public class Prova extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private String disciplina;

	@Column(nullable = false)
	private Double nota;

	@Column(nullable = false)
	private Integer av;

	@Column(nullable = false)
	private String assunto;

	@ManyToOne
	@JoinColumn(name = "professor_id", nullable = false)
	private Professor professor;

	@Column(nullable = false)
	private String turma;

	@ManyToOne
	@JoinColumn(name = "aluno_id", nullable = false)
	private Aluno aluno;

	public String getDisciplina() {
		return disciplina;
	}

	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}

	public Double getNota() {
		return nota;
	}

	public void setNota(Double nota) {
		this.nota = nota;
	}

	public Integer getAv() {
		return av;
	}

	public void setAv(Integer av) {
		this.av = av;
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public String getTurma() {
		return turma;
	}

	public void setTurma(String turma) {
		this.turma = turma;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

}