package com.example.demo.services.email;

/** Envio do código do 2º fator. Implementações: console, resend (HTTP) e smtp. */
public interface EmailService {

	void enviarCodigo(String paraEmail, String nome, String codigo, int validadeMinutos);
}
