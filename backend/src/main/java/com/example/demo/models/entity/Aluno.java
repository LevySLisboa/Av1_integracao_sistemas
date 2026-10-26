package com.example.demo.models.entity;

import java.util.Date;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.demo.models.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "aluno")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE aluno SET ativo = false WHERE id = ?")
public class Aluno extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	@NotBlank @Size(max = 150)
	private String nome;

	@Column(nullable = false, unique = true)
	@NotBlank @Email @Size(max = 150)
	private String email;

	@Column(unique = true)
	private String matricula;

	@Column(name = "data_nascimento", nullable = false)
	@JsonFormat(pattern = "dd/MM/yyyy", timezone = "America/Sao_Paulo")
	@NotNull @Past
	private Date dataNascimento;

	@Column(nullable = false)
	@NotBlank @Size(max = 100)
	private String curso;

	@Column(name = "cpf_cnpj", nullable = false)
	@NotBlank @Size(max = 18)
	private String cpfCnpj;

	@Column(name = "semestre_de_referencia", nullable = false)
	@NotNull @Min(1)
	private Integer semestreDeReferencia;

	@Column(nullable = false)
	@NotBlank @Size(max = 20)
	private String telefone;

	@Column(name = "pontuacao_media_geral", nullable = false)
	@NotNull @PositiveOrZero
	private Double pontuacaoMediaGeral;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getCurso() {
		return curso;
	}

	public void setCurso(String curso) {
		this.curso = curso;
	}

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public Integer getSemestreDeReferencia() {
		return semestreDeReferencia;
	}

	public void setSemestreDeReferencia(Integer semestreDeReferencia) {
		this.semestreDeReferencia = semestreDeReferencia;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public Double getPontuacaoMediaGeral() {
		return pontuacaoMediaGeral;
	}

	public void setPontuacaoMediaGeral(Double pontuacaoMediaGeral) {
		this.pontuacaoMediaGeral = pontuacaoMediaGeral;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
