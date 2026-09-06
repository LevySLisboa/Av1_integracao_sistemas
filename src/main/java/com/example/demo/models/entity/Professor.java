package com.example.demo.models.entity;

import java.util.Date;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.demo.models.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE professor SET ativo = false WHERE id = ?")
public class Professor extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(unique = true)
	private String matricula;

	@Column(name = "data_nascimento", nullable = false)
	@JsonFormat(pattern = "dd/MM/yyyy", timezone = "America/Sao_Paulo")
	private Date dataNascimento;

	@Column(name = "cpf_cnpj", nullable = false, unique = true)
	private String cpfCnpj;

	@Column(nullable = false)
	private String telefone;

	@Column(nullable = false)
	private String departamento;

	@Column(nullable = false)
	private String especializacao;

	@Column(nullable = false)
	private String titulacao;

	@Column(nullable = false)
	private Double salario;

	@Column(name = "carga_horaria", nullable = false)
	private Integer cargaHoraria;

	@Column(name = "data_admissao", nullable = false)
	@JsonFormat(pattern = "dd/MM/yyyy", timezone = "America/Sao_Paulo")
	private Date dataAdmissao;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getCpfCnpj() {
		return cpfCnpj;
	}

	public void setCpfCnpj(String cpfCnpj) {
		this.cpfCnpj = cpfCnpj;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public String getEspecializacao() {
		return especializacao;
	}

	public void setEspecializacao(String especializacao) {
		this.especializacao = especializacao;
	}

	public String getTitulacao() {
		return titulacao;
	}

	public void setTitulacao(String titulacao) {
		this.titulacao = titulacao;
	}

	public Double getSalario() {
		return salario;
	}

	public void setSalario(Double salario) {
		this.salario = salario;
	}

	public Integer getCargaHoraria() {
		return cargaHoraria;
	}

	public void setCargaHoraria(Integer cargaHoraria) {
		this.cargaHoraria = cargaHoraria;
	}

	public Date getDataAdmissao() {
		return dataAdmissao;
	}

	public void setDataAdmissao(Date dataAdmissao) {
		this.dataAdmissao = dataAdmissao;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
