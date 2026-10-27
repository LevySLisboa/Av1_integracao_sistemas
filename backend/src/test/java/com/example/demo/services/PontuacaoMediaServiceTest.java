package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Prova;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Provas;

@ExtendWith(MockitoExtension.class)
class PontuacaoMediaServiceTest {

	@Mock
	private Alunos alunos;

	@Mock
	private Provas provas;

	@InjectMocks
	private PontuacaoMediaService pontuacaoMedia;

	@Test
	void calculaMediaDasDisciplinasCompletasEIgnoraIncompletas() {
		Aluno aluno = new Aluno();
		aluno.setMatricula("A1");
		when(provas.findByAluno_Matricula("A1")).thenReturn(List.of(
				prova("Matemática", 1, 8), prova("Matemática", 2, 6), prova("Matemática", 3, 10),
				prova("História", 1, 2), prova("História", 2, 4), prova("História", 3, 6),
				prova("Biologia", 1, 10)));

		pontuacaoMedia.recalcular(aluno);

		assertEquals(6.5, aluno.getPontuacaoMediaGeral(), 0.000001);
		verify(alunos).save(aluno);
	}

	@Test
	void usaZeroQuandoNaoHaDisciplinaCompleta() {
		Aluno aluno = new Aluno();
		aluno.setMatricula("A1");
		when(provas.findByAluno_Matricula("A1"))
				.thenReturn(List.of(prova("Matemática", 1, 8), prova("Matemática", 2, 6)));

		pontuacaoMedia.recalcular(aluno);

		assertEquals(0.0, aluno.getPontuacaoMediaGeral());
	}

	private Prova prova(String disciplina, int av, double nota) {
		Prova prova = new Prova();
		prova.setDisciplina(disciplina);
		prova.setAv(av);
		prova.setNota(nota);
		return prova;
	}
}
