package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class MuitasTentativasException extends ApiException {

	private static final long serialVersionUID = 1L;

	public MuitasTentativasException(String mensagem) {
		super(HttpStatus.TOO_MANY_REQUESTS, mensagem);
	}

	public MuitasTentativasException(String mensagem, Throwable causa) {
		super(HttpStatus.TOO_MANY_REQUESTS, mensagem, causa);
	}
}
