package com.example.demo.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CodigoRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.security.UsuarioPrincipal;
import com.example.demo.services.AutenticacaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Endpoints de autenticação. O logout (POST /auth/logout) é tratado pelo filtro
 * de logout do Spring Security (ver SecurityConfig).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AutenticacaoService autenticacao;

	public AuthController(AutenticacaoService autenticacao) {
		this.autenticacao = autenticacao;
	}

	/** Entrega o token CSRF (também gravado no cookie XSRF-TOKEN). */
	@GetMapping("/csrf")
	public Map<String, String> csrf(CsrfToken token) {
		return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
	}

	/** Etapa 1: email + senha. Se corretos, envia o código por email. */
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req,
			HttpServletRequest request) {
		return ResponseEntity.ok(autenticacao.iniciarLogin(req, request));
	}

	/** Etapa 2: código recebido por email. Se correto, a sessão passa a ser autenticada. */
	@PostMapping("/verificar-2fa")
	public ResponseEntity<Map<String, Object>> verificar(@Valid @RequestBody CodigoRequest req,
			HttpServletRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(autenticacao.verificarCodigo(req, request, response));
	}

	@PostMapping("/reenviar-codigo")
	public ResponseEntity<Map<String, Object>> reenviar(HttpServletRequest request) {
		return ResponseEntity.ok(autenticacao.reenviarCodigo(request));
	}

	@GetMapping("/me")
	public Map<String, Object> me(@AuthenticationPrincipal UsuarioPrincipal usuario) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("id", usuario.id());
		corpo.put("nome", usuario.nome());
		corpo.put("email", usuario.email());
		corpo.put("papel", usuario.papel());
		corpo.put("matricula", usuario.matricula());
		return corpo;
	}
}
