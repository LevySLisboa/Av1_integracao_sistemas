package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Prova;
import com.example.demo.models.repository.Alunos;
import com.example.demo.models.repository.Provas;

@Service
public class PontuacaoMediaService {

	private final Alunos alunos;
	private final Provas provas;

	public PontuacaoMediaService(Alunos alunos, Provas provas) {
		this.alunos = alunos;
		this.provas = provas;
	}

	@Transactional
	public void recalcular(Aluno aluno) {
		Map<String, Map<Integer, List<Double>>> notasPorDisciplina = new HashMap<>();
		for (Prova prova : provas.findByAluno_Matricula(aluno.getMatricula())) {
			notasPorDisciplina.computeIfAbsent(prova.getDisciplina(), disciplina -> new HashMap<>())
					.computeIfAbsent(prova.getAv(), av -> new ArrayList<>())
					.add(prova.getNota());
		}

		double somaDasMedias = 0;
		int disciplinasCompletas = 0;
		for (Map<Integer, List<Double>> notasPorAv : notasPorDisciplina.values()) {
			if (!notasPorAv.keySet().containsAll(List.of(1, 2, 3))) {
				continue;
			}

			double av1 = media(notasPorAv.get(1));
			double av2 = media(notasPorAv.get(2));
			double av3 = media(notasPorAv.get(3));
			somaDasMedias += ((av1 / 2) + (av2 / 2) + av3) / 2;
			disciplinasCompletas++;
		}

		aluno.setPontuacaoMediaGeral(disciplinasCompletas == 0 ? 0 : somaDasMedias / disciplinasCompletas);
		alunos.save(aluno);
	}

	@Transactional
	public void recalcularTodos() {
		for (Aluno aluno : alunos.findAll()) {
			recalcular(aluno);
		}
	}

	private double media(List<Double> notas) {
		return notas.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
	}
}
