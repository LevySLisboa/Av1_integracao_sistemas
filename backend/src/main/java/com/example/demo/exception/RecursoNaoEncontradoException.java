package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public RecursoNaoEncontradoException(String mensagem) {
		super(HttpStatus.NOT_FOUND, mensagem);
	}

	public RecursoNaoEncontradoException(String mensagem, Throwable causa) {
		super(HttpStatus.NOT_FOUND, mensagem, causa);
	}
}
