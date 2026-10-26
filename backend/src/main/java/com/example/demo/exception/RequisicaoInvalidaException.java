package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends ApiException {

	private static final long serialVersionUID = 1L;

	public RequisicaoInvalidaException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, mensagem);
	}

	public RequisicaoInvalidaException(String mensagem, Throwable causa) {
		super(HttpStatus.BAD_REQUEST, mensagem, causa);
	}
}
