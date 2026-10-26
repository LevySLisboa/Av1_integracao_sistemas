package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class ServicoExternoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ServicoExternoException(String mensagem) {
		super(HttpStatus.BAD_GATEWAY, mensagem);
	}

	public ServicoExternoException(String mensagem, Throwable causa) {
		super(HttpStatus.BAD_GATEWAY, mensagem, causa);
	}
}
