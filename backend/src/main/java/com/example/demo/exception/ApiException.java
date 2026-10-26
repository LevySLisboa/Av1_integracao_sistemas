package com.example.demo.exception;

import org.springframework.http.HttpStatus;

/** Erro de negócio com status HTTP associado (tratado em ErroHandler). */
public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status;

	public ApiException(HttpStatus status, String mensagem) {
		super(mensagem);
		this.status = status;
	}

	public ApiException(HttpStatus status, String mensagem, Throwable causa) {
		super(mensagem, causa);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
