package com.example.demo.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

/** Escreve respostas JSON de erro/sucesso fora do MVC (filtros de segurança). */
public final class JsonErro {

	private JsonErro() {
	}

	public static void escrever(HttpServletResponse res, int status, String erro, String mensagem)
			throws IOException {
		res.setStatus(status);
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(String.format("{\"timestamp\":\"%s\",\"status\":%d,\"erro\":\"%s\",\"mensagem\":\"%s\"}",
				Instant.now(), status, esc(erro), esc(mensagem)));
	}

	public static void mensagem(HttpServletResponse res, int status, String mensagem) throws IOException {
		res.setStatus(status);
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(String.format("{\"mensagem\":\"%s\"}", esc(mensagem)));
	}

	private static String esc(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
