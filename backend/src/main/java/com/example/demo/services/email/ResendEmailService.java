package com.example.demo.services.email;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.demo.exception.EmailFalhouException;

/**
 * Envia email pela API HTTP do Resend (https://resend.com). Usa HTTPS (porta
 * 443), por isso funciona no plano gratuito do Render, que bloqueia SMTP.
 */
@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "resend")
public class ResendEmailService implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

	private final RestClient http;
	private final String apiKey;
	private final String remetente;

	public ResendEmailService(RestClient restClientExterno, @Value("${app.mail.resend-api-key:}") String apiKey,
			@Value("${app.mail.from}") String remetente) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("MAIL_PROVIDER=resend exige a variável RESEND_API_KEY.");
		}
		this.http = restClientExterno;
		this.apiKey = apiKey;
		this.remetente = remetente;
	}

	@Override
	public void enviarCodigo(String paraEmail, String nome, String codigo, int validadeMinutos) {
		String texto = "Olá, " + nome + "!\n\nSeu código de verificação é: " + codigo + "\n\nEle expira em "
				+ validadeMinutos + " minutos. Se não foi você quem tentou entrar, ignore este email e troque sua senha.";
		try {
			http.post().uri("https://api.resend.com/emails")
				.header("Authorization", "Bearer " + apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("from", remetente, "to", List.of(paraEmail), "subject", "Seu código de verificação",
						"text", texto))
				.retrieve().toBodilessEntity();
		} catch (RestClientException e) {
			log.error("Falha ao enviar email via Resend: {}", e.getMessage());
			throw new EmailFalhouException("Não foi possível enviar o código por email. Tente novamente.", e);
		}
	}
}
