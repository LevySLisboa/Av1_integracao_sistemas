package com.example.demo.controllers;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.entity.Aluno;
import com.example.demo.models.repository.Alunos;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

	@Autowired
	private Alunos alunos;

	@GetMapping({ "", "/" })
	public ResponseEntity<List<Aluno>> listar() {
		return ResponseEntity.ok(alunos.findAll());
	}

	@GetMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Aluno> buscarPorMatricula(@PathVariable Integer matricula) throws Exception {
		Aluno aluno = alunos.findByMatricula(matricula);

		if (aluno == null) {
			throw new Exception("Não existe Aluno com essa matricula");
		}

		return ResponseEntity.ok(aluno);
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Aluno> criarAluno(@RequestBody Aluno aluno) throws Exception {
		if (aluno == null) {
			throw new Exception("Não foi possivel criar um aluno com esses dados");
		}

		Aluno novoAluno = alunos.save(aluno);

		SimpleDateFormat pegarAno = new SimpleDateFormat("yyyy");
		String ano = pegarAno.format(novoAluno.getDataCriacao());

		String matriculaGerada = novoAluno.getId() + "" + ano;

		novoAluno.setMatricula(matriculaGerada);

		alunos.save(novoAluno);
		return ResponseEntity.ok(novoAluno);
	}

	@PutMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Aluno> editarAluno(@PathVariable Integer matricula, @RequestBody Aluno aluno)
			throws Exception {

		Aluno alunoExistente = alunos.findByMatricula(matricula);

		if (alunoExistente == null) {
			throw new Exception("Não existe Aluno com essa matricula");
		}

		alunoExistente.setNome(aluno.getNome());
		alunoExistente.setEmail(aluno.getEmail());
		alunoExistente.setDataNascimento(aluno.getDataNascimento());
		alunoExistente.setCurso(aluno.getCurso());
		alunoExistente.setCpfCnpj(aluno.getCpfCnpj());
		alunoExistente.setSemestreDeReferencia(aluno.getSemestreDeReferencia());
		alunoExistente.setTelefone(aluno.getTelefone());
		alunoExistente.setPontuacaoMediaGeral(aluno.getPontuacaoMediaGeral());

		Aluno alunoAtualizado = alunos.save(alunoExistente);

		return ResponseEntity.ok(alunoAtualizado);
	}

	@DeleteMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Void> deletarAluno(@PathVariable Integer matricula) throws Exception {
		Aluno aluno = alunos.findByMatricula(matricula);

		if (aluno == null) {
			throw new Exception("Não existe Aluno com essa matricula");
		}

		alunos.delete(aluno);
		return ResponseEntity.noContent().build();
	}
}
