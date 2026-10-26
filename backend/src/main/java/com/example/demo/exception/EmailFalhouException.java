package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class EmailFalhouException extends ApiException {

	private static final long serialVersionUID = 1L;

	public EmailFalhouException(String mensagem) {
		super(HttpStatus.SERVICE_UNAVAILABLE, mensagem);
	}

	public EmailFalhouException(String mensagem, Throwable causa) {
		super(HttpStatus.SERVICE_UNAVAILABLE, mensagem, causa);
	}
}
