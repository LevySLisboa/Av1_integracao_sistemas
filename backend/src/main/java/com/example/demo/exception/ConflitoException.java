package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class ConflitoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ConflitoException(String mensagem) {
		super(HttpStatus.CONFLICT, mensagem);
	}

	public ConflitoException(String mensagem, Throwable causa) {
		super(HttpStatus.CONFLICT, mensagem, causa);
	}
}
