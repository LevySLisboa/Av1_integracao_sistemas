package com.example.demo.services.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.exception.EmailFalhouException;

/** Envio por SMTP. No Render, só funciona em instância paga (portas 465/587). */
@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp")
public class SmtpEmailService implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

	private final JavaMailSender sender;
	private final String remetente;

	public SmtpEmailService(JavaMailSender sender, @Value("${app.mail.from}") String remetente) {
		this.sender = sender;
		this.remetente = remetente;
	}

	@Override
	public void enviarCodigo(String paraEmail, String nome, String codigo, int validadeMinutos) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(remetente);
		msg.setTo(paraEmail);
		msg.setSubject("Seu código de verificação");
		msg.setText("Olá, " + nome + "!\n\nSeu código de verificação é: " + codigo + "\n\nEle expira em "
				+ validadeMinutos + " minutos. Se não foi você quem tentou entrar, ignore este email e troque sua senha.");
		try {
			sender.send(msg);
		} catch (MailException e) {
			log.error("Falha ao enviar email via SMTP: {}", e.getMessage());
			throw new EmailFalhouException("Não foi possível enviar o código por email. Tente novamente.", e);
		}
	}
}
