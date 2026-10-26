package com.example.demo.models.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Código OTP (2º fator). Só o HASH do código é persistido. */
@Entity
@Table(name = "codigo_verificacao")
public class CodigoVerificacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(name = "codigo_hash", nullable = false)
	private String codigoHash;

	@Column(name = "expira_em", nullable = false)
	private Instant expiraEm;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm = Instant.now();

	@Column(nullable = false)
	private int tentativas = 0;

	public CodigoVerificacao() {
	}

	public CodigoVerificacao(Long usuarioId, String codigoHash, Instant expiraEm) {
		this.usuarioId = usuarioId;
		this.codigoHash = codigoHash;
		this.expiraEm = expiraEm;
	}

	public Long getId() { return id; }
	public Long getUsuarioId() { return usuarioId; }
	public String getCodigoHash() { return codigoHash; }
	public Instant getExpiraEm() { return expiraEm; }
	public Instant getCriadoEm() { return criadoEm; }
	public int getTentativas() { return tentativas; }
	public void setTentativas(int tentativas) { this.tentativas = tentativas; }
}
