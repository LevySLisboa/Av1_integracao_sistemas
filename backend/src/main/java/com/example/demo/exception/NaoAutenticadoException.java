package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public NaoAutenticadoException(String mensagem) {
		super(HttpStatus.UNAUTHORIZED, mensagem);
	}

	public NaoAutenticadoException(String mensagem, Throwable causa) {
		super(HttpStatus.UNAUTHORIZED, mensagem, causa);
	}
}
