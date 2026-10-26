package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.example.demo.security.JsonErro;
import com.example.demo.security.RateLimitFilter;

/**
 * Autenticação por SESSÃO (cookie HttpOnly) com 2º fator por email.
 * Não usa Basic, Bearer/JWT, OAuth2 nem API Key.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
			@Value("${app.ratelimit.auth-por-minuto:10}") int authPorMinuto,
			@Value("${app.ratelimit.geral-por-minuto:120}") int geralPorMinuto) throws Exception {

		RateLimitFilter rateLimit = new RateLimitFilter(authPorMinuto, geralPorMinuto);

		http
			// CSRF: cookie XSRF-TOKEN legível pelo cliente + header X-XSRF-TOKEN
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.addFilterBefore(rateLimit, SecurityContextHolderFilter.class)

			.authorizeHttpRequests(auth -> auth
				// públicos (fluxo de login em 2 etapas)
				.requestMatchers("/auth/csrf", "/auth/login", "/auth/verificar-2fa", "/auth/reenviar-codigo")
					.permitAll()
				.requestMatchers("/auth/logout", "/auth/me").authenticated()

				// gestão de contas: só ADMIN
				.requestMatchers("/usuarios/**").hasRole("ADMIN")

				// integração com API externa
				.requestMatchers("/integracao/**").hasAnyRole("ADMIN", "PROFESSOR")

				// alunos
				.requestMatchers(HttpMethod.GET, "/alunos", "/alunos/").hasAnyRole("ADMIN", "PROFESSOR")
				.requestMatchers(HttpMethod.GET, "/alunos/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
				.requestMatchers("/alunos/**").hasRole("ADMIN")

				// professores
				.requestMatchers(HttpMethod.GET, "/professor/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
				.requestMatchers("/professor/**").hasRole("ADMIN")

				// provas
				.requestMatchers(HttpMethod.GET, "/provas/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
				.requestMatchers("/provas/**").hasAnyRole("ADMIN", "PROFESSOR")

				.anyRequest().authenticated())

			.exceptionHandling(e -> e
				// 401: sem sessão autenticada
				.authenticationEntryPoint((req, res, ex) -> JsonErro.escrever(res, 401, "Unauthorized",
						"Autenticação necessária. Faça login em POST /auth/login e confirme o código enviado por email."))
				// 403: autenticado, mas sem permissão (ou CSRF inválido)
				.accessDeniedHandler((req, res, ex) -> {
					if (ex instanceof CsrfException) {
						JsonErro.escrever(res, 403, "Forbidden",
								"Token CSRF ausente ou inválido. Obtenha em GET /auth/csrf e envie no header X-XSRF-TOKEN.");
					} else {
						JsonErro.escrever(res, 403, "Forbidden",
								"Acesso negado: seu perfil não tem permissão para este recurso.");
					}
				}))

			.logout(l -> l
				.logoutUrl("/auth/logout")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("SESSION")
				.logoutSuccessHandler((req, res, auth) -> JsonErro.mensagem(res, 200, "Logout realizado com sucesso.")));

		return http.build();
	}
}
