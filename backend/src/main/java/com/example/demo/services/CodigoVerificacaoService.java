package com.example.demo.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.models.entity.CodigoVerificacao;
import com.example.demo.models.repository.CodigosVerificacao;

/**
 * Gera e valida o código OTP de 6 dígitos (2º fator).
 * - gerado com SecureRandom;
 * - só o hash (BCrypt) é persistido;
 * - expira (padrão 5 min) e tem número máximo de tentativas (padrão 5);
 * - é de uso único (apagado após validar).
 */
@Service
public class CodigoVerificacaoService {

	public enum Resultado {
		OK, INVALIDO, EXPIRADO_OU_INEXISTENTE, TENTATIVAS_ESGOTADAS
	}

	private final CodigosVerificacao repo;
	private final PasswordEncoder encoder;
	private final SecureRandom random = new SecureRandom();
	private final long ttlSegundos;
	private final int maxTentativas;
	private final long cooldownSegundos;

	public CodigoVerificacaoService(CodigosVerificacao repo, PasswordEncoder encoder,
			@Value("${app.twofa.ttl-segundos:300}") long ttlSegundos,
			@Value("${app.twofa.max-tentativas:5}") int maxTentativas,
			@Value("${app.twofa.reenvio-cooldown-segundos:60}") long cooldownSegundos) {
		this.repo = repo;
		this.encoder = encoder;
		this.ttlSegundos = ttlSegundos;
		this.maxTentativas = maxTentativas;
		this.cooldownSegundos = cooldownSegundos;
	}

	public long getTtlSegundos() {
		return ttlSegundos;
	}

	/** Cria um novo código (invalidando os anteriores) e devolve o valor em texto para envio. */
	@Transactional
	public String gerar(Long usuarioId) {
		repo.deleteByExpiraEmBefore(Instant.now());
		repo.deleteByUsuarioId(usuarioId);
		String codigo = String.format("%06d", random.nextInt(1_000_000));
		repo.save(new CodigoVerificacao(usuarioId, encoder.encode(codigo), Instant.now().plusSeconds(ttlSegundos)));
		return codigo;
	}

	@Transactional
	public void descartar(Long usuarioId) {
		repo.deleteByUsuarioId(usuarioId);
	}

	@Transactional
	public Resultado validar(Long usuarioId, String codigoInformado) {
		Optional<CodigoVerificacao> opt = repo.findFirstByUsuarioIdOrderByCriadoEmDesc(usuarioId);
		if (opt.isEmpty()) {
			return Resultado.EXPIRADO_OU_INEXISTENTE;
		}
		CodigoVerificacao cv = opt.get();

		if (cv.getExpiraEm().isBefore(Instant.now())) {
			repo.delete(cv);
			return Resultado.EXPIRADO_OU_INEXISTENTE;
		}
		if (cv.getTentativas() >= maxTentativas) {
			repo.delete(cv);
			return Resultado.TENTATIVAS_ESGOTADAS;
		}
		if (!encoder.matches(codigoInformado, cv.getCodigoHash())) {
			cv.setTentativas(cv.getTentativas() + 1);
			if (cv.getTentativas() >= maxTentativas) {
				repo.delete(cv);
				return Resultado.TENTATIVAS_ESGOTADAS;
			}
			repo.save(cv);
			return Resultado.INVALIDO;
		}
		repo.delete(cv); // uso único
		return Resultado.OK;
	}

	/** Segundos que faltam para poder pedir novo código (0 = já pode). */
	public long segundosParaReenvio(Long usuarioId) {
		return repo.findFirstByUsuarioIdOrderByCriadoEmDesc(usuarioId).map(cv -> {
			long restante = Duration.between(Instant.now(), cv.getCriadoEm().plusSeconds(cooldownSegundos))
					.toSeconds();
			return Math.max(0, restante);
		}).orElse(0L);
	}
}
