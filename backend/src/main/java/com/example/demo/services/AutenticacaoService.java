package com.example.demo.services;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CodigoRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.MuitasTentativasException;
import com.example.demo.exception.NaoAutenticadoException;
import com.example.demo.models.entity.Usuario;
import com.example.demo.models.repository.Usuarios;
import com.example.demo.security.Mascara;
import com.example.demo.security.UsuarioPrincipal;
import com.example.demo.services.CodigoVerificacaoService.Resultado;
import com.example.demo.services.email.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Fluxo de login em duas etapas, baseado em sessão:
 *  1) login (email+senha)  -> sessão "pré-autenticada" + código por email
 *  2) verificar-2fa        -> sessão promovida a autenticada
 */
@Service
public class AutenticacaoService {

	static final String ATTR_PRE_AUTH_USUARIO = "PRE_AUTH_USUARIO_ID";
	static final String ATTR_PRE_AUTH_ATE = "PRE_AUTH_ATE_MS";

	private static final String MSG_CREDENCIAIS = "Credenciais inválidas.";

	private final Usuarios usuarios;
	private final CodigoVerificacaoService codigos;
	private final EmailService email;
	private final PasswordEncoder encoder;
	private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
	private final String hashFicticio;
	private final int maxFalhas;
	private final long bloqueioMinutos;

	public AutenticacaoService(Usuarios usuarios, CodigoVerificacaoService codigos, EmailService email,
			PasswordEncoder encoder, @Value("${app.auth.max-falhas-login:5}") int maxFalhas,
			@Value("${app.auth.bloqueio-minutos:15}") long bloqueioMinutos) {
		this.usuarios = usuarios;
		this.codigos = codigos;
		this.email = email;
		this.encoder = encoder;
		this.maxFalhas = maxFalhas;
		this.bloqueioMinutos = bloqueioMinutos;
		// usado para gastar o mesmo tempo de BCrypt quando o email não existe (evita enumeração por tempo)
		this.hashFicticio = encoder.encode("senha-ficticia-para-equalizar-tempo");
	}

	// ---------------------------------------------------------------- etapa 1
	public Map<String, Object> iniciarLogin(LoginRequest req, HttpServletRequest request) {
		String emailNormalizado = req.email().trim().toLowerCase();
		Usuario u = usuarios.findByEmail(emailNormalizado).orElse(null);

		if (u == null) {
			encoder.matches(req.senha(), hashFicticio);
			throw new NaoAutenticadoException(MSG_CREDENCIAIS);
		}
		if (u.getBloqueadoAte() != null && u.getBloqueadoAte().isAfter(Instant.now())) {
			throw new MuitasTentativasException("Conta temporariamente bloqueada por excesso de tentativas. Tente novamente mais tarde.");
		}
		if (!encoder.matches(req.senha(), u.getSenhaHash())) {
			registrarFalha(u);
			throw new NaoAutenticadoException(MSG_CREDENCIAIS);
		}

		u.setTentativasFalhas(0);
		u.setBloqueadoAte(null);
		usuarios.save(u);

		// descarta qualquer sessão anterior e abre uma nova (pré-autenticada, sem acesso)
		HttpSession antiga = request.getSession(false);
		if (antiga != null) {
			antiga.invalidate();
		}
		HttpSession sessao = request.getSession(true);

		enviarCodigo(u);

		sessao.setAttribute(ATTR_PRE_AUTH_USUARIO, u.getId());
		sessao.setAttribute(ATTR_PRE_AUTH_ATE, System.currentTimeMillis() + codigos.getTtlSegundos() * 1000);

		return respostaCodigoEnviado(u);
	}

	// ---------------------------------------------------------------- etapa 2
	public Map<String, Object> verificarCodigo(CodigoRequest req, HttpServletRequest request,
			HttpServletResponse response) {

		HttpSession sessao = request.getSession(false);
		Usuario u = usuarioPreAutenticado(sessao);

		Resultado r = codigos.validar(u.getId(), req.codigo());
		switch (r) {
		case OK -> {
		}
		case INVALIDO -> throw new NaoAutenticadoException("Código inválido.");
		case EXPIRADO_OU_INEXISTENTE -> {
			sessao.invalidate();
			throw new NaoAutenticadoException("Código expirado. Faça login novamente.");
		}
		case TENTATIVAS_ESGOTADAS -> {
			sessao.invalidate();
			throw new NaoAutenticadoException("Número máximo de tentativas excedido. Faça login novamente.");
		}
		}

		// Sucesso: promove a sessão. changeSessionId evita session fixation.
		request.changeSessionId();
		sessao.removeAttribute(ATTR_PRE_AUTH_USUARIO);
		sessao.removeAttribute(ATTR_PRE_AUTH_ATE);

		UsuarioPrincipal principal = new UsuarioPrincipal(u.getId(), u.getEmail(), u.getNome(), u.getPapel(),
				u.getMatricula());
		var autenticacao = UsernamePasswordAuthenticationToken.authenticated(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_" + u.getPapel().name())));
		SecurityContext contexto = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
		contexto.setAuthentication(autenticacao);
		SecurityContextHolder.getContextHolderStrategy().setContext(contexto);
		contextRepository.saveContext(contexto, request, response);

		u.setUltimoLogin(Instant.now());
		usuarios.save(u);

		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("mensagem", "Autenticado com sucesso.");
		corpo.put("usuario", principal);
		return corpo;
	}

	// ------------------------------------------------------------- reenviar
	public Map<String, Object> reenviarCodigo(HttpServletRequest request) {
		HttpSession sessao = request.getSession(false);
		Usuario u = usuarioPreAutenticado(sessao);

		long espera = codigos.segundosParaReenvio(u.getId());
		if (espera > 0) {
			throw new MuitasTentativasException("Aguarde " + espera + " segundos para solicitar um novo código.");
		}
		enviarCodigo(u);
		sessao.setAttribute(ATTR_PRE_AUTH_ATE, System.currentTimeMillis() + codigos.getTtlSegundos() * 1000);
		return respostaCodigoEnviado(u);
	}

	// -------------------------------------------------------------- helpers
	private Usuario usuarioPreAutenticado(HttpSession sessao) {
		String msg = "Nenhum login em andamento ou sessão expirada. Faça login em POST /auth/login.";
		if (sessao == null) {
			throw new NaoAutenticadoException(msg);
		}
		Object id = sessao.getAttribute(ATTR_PRE_AUTH_USUARIO);
		Object ate = sessao.getAttribute(ATTR_PRE_AUTH_ATE);
		if (!(id instanceof Long userId) || !(ate instanceof Long limite) || System.currentTimeMillis() > limite) {
			sessao.invalidate();
			throw new NaoAutenticadoException(msg);
		}
		return usuarios.findById(userId).orElseThrow(() -> new NaoAutenticadoException(msg));
	}

	private void enviarCodigo(Usuario u) {
		String codigo = codigos.gerar(u.getId());
		try {
			email.enviarCodigo(u.getEmail(), u.getNome(), codigo, (int) (codigos.getTtlSegundos() / 60));
		} catch (RuntimeException e) {
			codigos.descartar(u.getId());
			throw e;
		}
	}

	private void registrarFalha(Usuario u) {
		int falhas = u.getTentativasFalhas() + 1;
		if (falhas >= maxFalhas) {
			u.setBloqueadoAte(Instant.now().plusSeconds(bloqueioMinutos * 60));
			falhas = 0;
		}
		u.setTentativasFalhas(falhas);
		usuarios.save(u);
	}

	private Map<String, Object> respostaCodigoEnviado(Usuario u) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("mensagem", "Senha correta. Enviamos um código de 6 dígitos para " + Mascara.email(u.getEmail()) + ".");
		corpo.put("doisFatoresNecessario", true);
		corpo.put("codigoExpiraEmSegundos", codigos.getTtlSegundos());
		corpo.put("proximoPasso", "POST /auth/verificar-2fa com {\"codigo\":\"123456\"}");
		return corpo;
	}
}
