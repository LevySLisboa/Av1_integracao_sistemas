package com.example.demo.services.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** SOMENTE DESENVOLVIMENTO: imprime o código no log em vez de enviar email. */
@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailService implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

	@Override
	public void enviarCodigo(String paraEmail, String nome, String codigo, int validadeMinutos) {
		log.warn("[MODO DEV - NÃO USE EM PRODUÇÃO] Código 2FA para {}: {} (válido por {} min)", paraEmail, codigo,
				validadeMinutos);
	}
}
