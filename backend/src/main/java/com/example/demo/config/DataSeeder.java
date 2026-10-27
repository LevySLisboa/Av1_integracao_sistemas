package com.example.demo.config;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Papel;
import com.example.demo.models.entity.Professor;
import com.example.demo.models.entity.Prova;
import com.example.demo.models.entity.Usuario;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Professores;
import com.example.demo.models.repository.Provas;
import com.example.demo.models.repository.Usuarios;
import com.example.demo.services.PontuacaoMediaService;

/**
 * Popula o banco na primeira execução: 3 professores, 3 alunos, 9 provas e
 * (se as variáveis SEED_* estiverem definidas) usuários de demonstração.
 * Os dados pessoais abaixo são FICTÍCIOS. Senhas vêm do ambiente, nunca do código.
 */
@Component
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	private final Professores professores;
	private final Alunos alunos;
	private final Provas provas;
	private final Usuarios usuarios;
	private final PasswordEncoder encoder;
	private final PontuacaoMediaService pontuacaoMedia;

	@Value("${app.seed.admin-email:}")
	private String adminEmail;
	@Value("${app.seed.admin-password:}")
	private String adminSenha;
	@Value("${app.seed.professor-email:}")
	private String professorEmail;
	@Value("${app.seed.aluno-email:}")
	private String alunoEmail;
	@Value("${app.seed.user-password:}")
	private String userSenha;

	public DataSeeder(Professores professores, Alunos alunos, Provas provas, Usuarios usuarios,
			PasswordEncoder encoder, PontuacaoMediaService pontuacaoMedia) {
		this.professores = professores;
		this.alunos = alunos;
		this.provas = provas;
		this.usuarios = usuarios;
		this.encoder = encoder;
		this.pontuacaoMedia = pontuacaoMedia;
	}

	@Override
	public void run(String... args) {
		if (professores.count() == 0) {
			criarProfessor("Carlos Mendes", "carlos.mendes@exemplo.edu", "111.111.111-11", "Computação", "Redes", "Mestre", 9500.0, 40, LocalDate.of(1980, 3, 12), LocalDate.of(2015, 2, 1));
			criarProfessor("Fernanda Lima", "fernanda.lima@exemplo.edu", "222.222.222-22", "Computação", "Banco de Dados", "Doutora", 12000.0, 40, LocalDate.of(1975, 7, 30), LocalDate.of(2010, 8, 15));
			criarProfessor("Roberto Alves", "roberto.alves@exemplo.edu", "333.333.333-33", "Matemática", "Estatística", "Doutor", 11000.0, 20, LocalDate.of(1983, 11, 5), LocalDate.of(2018, 1, 20));
		}
		if (alunos.count() == 0) {
			criarAluno("Ana Souza", "ana.souza@exemplo.edu", "Ciência da Computação", "444.444.444-44", 5, "(85) 99999-0001", LocalDate.of(2003, 4, 10));
			criarAluno("Bruno Costa", "bruno.costa@exemplo.edu", "Sistemas de Informação", "555.555.555-55", 3, "(85) 99999-0002", LocalDate.of(2004, 9, 22));
			criarAluno("Carla Dias", "carla.dias@exemplo.edu", "Engenharia de Software", "666.666.666-66", 7, "(85) 99999-0003", LocalDate.of(2002, 1, 15));
		}
		if (provas.count() == 0 && professores.count() >= 3 && alunos.count() >= 3) {
			var p = professores.findAll();
			var a = alunos.findAll();
			// 02/11/2026 é feriado nacional (Finados): útil para demonstrar a integração
			criarProva("Técnicas de Integração de Sistemas", 8.0, 1, "Fundamentos de integração", p.get(0), "N1", a.get(0), LocalDate.of(2026, 10, 26));
			criarProva("Técnicas de Integração de Sistemas", 8.5, 2, "Autenticação e APIs REST", p.get(0), "N1", a.get(0), LocalDate.of(2026, 11, 2));
			criarProva("Técnicas de Integração de Sistemas", 9.0, 3, "Integração de serviços", p.get(0), "N1", a.get(0), LocalDate.of(2026, 11, 16));

			criarProva("Banco de Dados", 7.0, 1, "Modelagem relacional", p.get(1), "N2", a.get(1), LocalDate.of(2026, 10, 27));
			criarProva("Banco de Dados", 7.5, 2, "Consultas SQL", p.get(1), "N2", a.get(1), LocalDate.of(2026, 11, 10));
			criarProva("Banco de Dados", 8.0, 3, "Normalização", p.get(1), "N2", a.get(1), LocalDate.of(2026, 11, 24));

			criarProva("Estatística Aplicada", 8.0, 1, "Estatística descritiva", p.get(2), "N1", a.get(2), LocalDate.of(2026, 10, 28));
			criarProva("Estatística Aplicada", 8.5, 2, "Distribuições", p.get(2), "N1", a.get(2), LocalDate.of(2026, 11, 11));
			criarProva("Estatística Aplicada", 9.0, 3, "Probabilidade", p.get(2), "N1", a.get(2), LocalDate.of(2026, 12, 7));
		}
		criarUsuarios();
		pontuacaoMedia.recalcularTodos();
	}

	private void criarUsuarios() {
		if (valido(adminEmail, adminSenha)) {
			garantirUsuario("Administrador", adminEmail, adminSenha, Papel.ADMIN, null);
		} else {
			log.warn("SEED_ADMIN_EMAIL/SEED_ADMIN_PASSWORD (mín. 10 caracteres) não definidos: admin não criado.");
		}
		if (valido(professorEmail, userSenha)) {
			var prof = professores.findAll().stream().findFirst();
			prof.ifPresent(p -> garantirUsuario(p.getNome(), professorEmail, userSenha, Papel.PROFESSOR, p.getMatricula()));
		}
		if (valido(alunoEmail, userSenha)) {
			var aluno = alunos.findAll().stream().findFirst();
			aluno.ifPresent(a -> garantirUsuario(a.getNome(), alunoEmail, userSenha, Papel.ALUNO, a.getMatricula()));
		}
	}

	private boolean valido(String email, String senha) {
		return email != null && !email.isBlank() && senha != null && senha.length() >= 10;
	}

	private void garantirUsuario(String nome, String email, String senha, Papel papel, String matricula) {
		String emailNormalizado = email.trim().toLowerCase();
		if (usuarios.existsByEmail(emailNormalizado)) {
			return;
		}
		Usuario u = new Usuario();
		u.setNome(nome);
		u.setEmail(emailNormalizado);
		u.setSenhaHash(encoder.encode(senha));
		u.setPapel(papel);
		u.setMatricula(matricula);
		usuarios.save(u);
		log.info("Usuário inicial criado: {} ({})", emailNormalizado, papel);
	}

	private Date data(LocalDate d) {
		return Date.from(d.atStartOfDay(FUSO).toInstant());
	}

	private void criarProfessor(String nome, String email, String cpf, String depto, String espec, String titulacao,
			double salario, int carga, LocalDate nasc, LocalDate admissao) {
		Professor p = new Professor();
		p.setNome(nome);
		p.setEmail(email);
		p.setCpfCnpj(cpf);
		p.setTelefone("(85) 98888-0000");
		p.setDepartamento(depto);
		p.setEspecializacao(espec);
		p.setTitulacao(titulacao);
		p.setSalario(salario);
		p.setCargaHoraria(carga);
		p.setDataNascimento(data(nasc));
		p.setDataAdmissao(data(admissao));
		p = professores.save(p);
		p.setMatricula(p.getId() + "" + Year.now().getValue());
		professores.save(p);
	}

	private void criarAluno(String nome, String email, String curso, String cpf, int semestre, String tel,
			LocalDate nasc) {
		Aluno a = new Aluno();
		a.setNome(nome);
		a.setEmail(email);
		a.setCurso(curso);
		a.setCpfCnpj(cpf);
		a.setSemestreDeReferencia(semestre);
		a.setTelefone(tel);
		a.setDataNascimento(data(nasc));
		a = alunos.save(a);
		a.setMatricula(a.getId() + "" + Year.now().getValue());
		alunos.save(a);
	}

	private void criarProva(String disciplina, double nota, int av, String assunto, Professor prof, String turma,
			Aluno aluno, LocalDate aplicacao) {
		Prova pr = new Prova();
		pr.setDisciplina(disciplina);
		pr.setNota(nota);
		pr.setAv(av);
		pr.setAssunto(assunto);
		pr.setProfessor(prof);
		pr.setTurma(turma);
		pr.setAluno(aluno);
		pr.setDataAplicacao(aplicacao);
		provas.save(pr);
	}
}
