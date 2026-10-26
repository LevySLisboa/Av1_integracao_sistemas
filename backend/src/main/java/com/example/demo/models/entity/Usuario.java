package com.example.demo.models.entity;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.demo.models.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Conta de login. Separada de Aluno/Professor (dados acadêmicos): aqui ficam
 * apenas credenciais e perfil de acesso. A senha NUNCA é guardada em texto
 * puro, só o hash BCrypt.
 */
@Entity
@Table(name = "usuario")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE usuario SET ativo = false WHERE id = ?")
public class Usuario extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

	@JsonIgnore
	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Papel papel;

	/** Matrícula do Aluno/Professor vinculado (null para ADMIN). */
	@Column
	private String matricula;

	@Column(name = "tentativas_falhas", nullable = false)
	private int tentativasFalhas = 0;

	@Column(name = "bloqueado_ate")
	private Instant bloqueadoAte;

	@Column(name = "ultimo_login")
	private Instant ultimoLogin;

	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getSenhaHash() { return senhaHash; }
	public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

	public Papel getPapel() { return papel; }
	public void setPapel(Papel papel) { this.papel = papel; }

	public String getMatricula() { return matricula; }
	public void setMatricula(String matricula) { this.matricula = matricula; }

	public int getTentativasFalhas() { return tentativasFalhas; }
	public void setTentativasFalhas(int tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }

	public Instant getBloqueadoAte() { return bloqueadoAte; }
	public void setBloqueadoAte(Instant bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

	public Instant getUltimoLogin() { return ultimoLogin; }
	public void setUltimoLogin(Instant ultimoLogin) { this.ultimoLogin = ultimoLogin; }
}
