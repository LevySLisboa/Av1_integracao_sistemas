package com.example.demo.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Limita requisições por IP: um limite geral para toda a API e um limite mais
 * rígido para os endpoints sensíveis de autenticação (anti força-bruta).
 */
public class RateLimitFilter extends OncePerRequestFilter {

	private static final Set<String> CAMINHOS_SENSIVEIS = Set.of("/auth/login", "/auth/verificar-2fa",
			"/auth/reenviar-codigo");

	private final LimitadorDeRequisicoes limitadorAuth;
	private final LimitadorDeRequisicoes limitadorGeral;

	public RateLimitFilter(int authPorMinuto, int geralPorMinuto) {
		this.limitadorAuth = new LimitadorDeRequisicoes(authPorMinuto, 60_000, System::currentTimeMillis);
		this.limitadorGeral = new LimitadorDeRequisicoes(geralPorMinuto, 60_000, System::currentTimeMillis);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String ip = request.getRemoteAddr();
		String caminho = request.getRequestURI();
		if (caminho.length() > 1 && caminho.endsWith("/")) {
			caminho = caminho.substring(0, caminho.length() - 1);
		}

		if (!limitadorGeral.permitir(ip)) {
			bloquear(response, limitadorGeral.segundosParaLiberar(ip));
			return;
		}
		if (CAMINHOS_SENSIVEIS.contains(caminho) && !limitadorAuth.permitir(ip)) {
			bloquear(response, limitadorAuth.segundosParaLiberar(ip));
			return;
		}
		chain.doFilter(request, response);
	}

	private void bloquear(HttpServletResponse response, long segundos) throws IOException {
		response.setHeader("Retry-After", String.valueOf(segundos));
		JsonErro.escrever(response, 429, "Too Many Requests",
				"Muitas requisições. Tente novamente em " + segundos + " segundos.");
	}
}
