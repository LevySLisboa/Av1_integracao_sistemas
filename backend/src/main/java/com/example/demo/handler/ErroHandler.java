package com.example.demo.handler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.demo.exception.ApiException;

/**
 * Converte exceções em respostas JSON padronizadas:
 * {timestamp, status, erro, mensagem[, campos]}. Erros inesperados NÃO vazam
 * detalhes internos (stack trace, SQL, etc.).
 */
@RestControllerAdvice
public class ErroHandler {

	private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> tratarApi(ApiException e) {
		return resposta(e.getStatus(), e.getMessage(), null);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AccessDeniedException e) {
		String msg = e.getMessage() != null && !e.getMessage().equals("Access Denied") ? e.getMessage()
				: "Acesso negado: seu perfil não tem permissão para este recurso.";
		return resposta(HttpStatus.FORBIDDEN, msg, null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, Object>> tratarAutenticacao(AuthenticationException e) {
		return resposta(HttpStatus.UNAUTHORIZED, "Autenticação necessária.", null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException e) {
		Map<String, String> campos = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
		return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", campos);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(Exception e) {
		return resposta(HttpStatus.BAD_REQUEST, "Requisição malformada ou com tipo de dado inválido.", null);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> tratarIntegridade(DataIntegrityViolationException e) {
		log.warn("Violação de integridade: {}", e.getMostSpecificCause().getMessage());
		return resposta(HttpStatus.CONFLICT, "Conflito de dados (registro duplicado ou referência inválida).", null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> tratarGenerico(Exception e) {
		// Exceções padrão do Spring MVC (404, 405, 415, parâmetro ausente...) já trazem status
		if (e instanceof ErrorResponse er) {
			HttpStatusCode status = er.getStatusCode();
			String detalhe = status.value() == 404 ? "Recurso não encontrado." : er.getBody().getDetail();
			return resposta(status, detalhe, null);
		}
		log.error("Erro inesperado", e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.", null);
	}

	private ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String mensagem,
			Map<String, String> campos) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("timestamp", Instant.now().toString());
		corpo.put("status", status.value());
		HttpStatus hs = HttpStatus.resolve(status.value());
		corpo.put("erro", hs != null ? hs.getReasonPhrase() : "Erro");
		corpo.put("mensagem", mensagem);
		if (campos != null) {
			corpo.put("campos", campos);
		}
		return ResponseEntity.status(status).body(corpo);
	}
}
