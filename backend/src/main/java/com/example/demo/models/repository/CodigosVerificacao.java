package com.example.demo.models.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.entity.CodigoVerificacao;

public interface CodigosVerificacao extends JpaRepository<CodigoVerificacao, Long> {

	Optional<CodigoVerificacao> findFirstByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

	void deleteByUsuarioId(Long usuarioId);

	void deleteByExpiraEmBefore(Instant limite);
}
