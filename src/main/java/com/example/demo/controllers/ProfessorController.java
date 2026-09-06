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
import com.example.demo.models.entity.Professor;
import com.example.demo.models.repository.Professores;

@RestController
@RequestMapping("/professor")
public class ProfessorController {

	@Autowired
	private Professores professores;

	@GetMapping({ "", "/" })
	public ResponseEntity<List<Professor>> listar() {
		return ResponseEntity.ok(professores.findAll());
	}

	@GetMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Professor> buscarPorMatricula(@PathVariable Integer matricula) throws Exception {
		Professor professor = professores.findByMatricula(matricula);

		if (professor == null) {
			throw new Exception("Não existe Aluno com essa matricula");
		}

		return ResponseEntity.ok(professor);
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Professor> criarProfessor(@RequestBody Professor professor) throws Exception {
		if (professor == null) {
			throw new Exception("Não foi possivel criar um professor com esses dados");
		}

		Professor novoProfessor = professores.save(professor);

		SimpleDateFormat pegarAno = new SimpleDateFormat("yyyy");
		String ano = pegarAno.format(novoProfessor.getDataCriacao());

		String matriculaGerada = novoProfessor.getId() + "" + ano;

		novoProfessor.setMatricula(matriculaGerada);

		professores.save(novoProfessor);
		return ResponseEntity.ok(novoProfessor);
	}

	@PutMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Professor> editarProfessor(@PathVariable Integer matricula, @RequestBody Professor professor)
			throws Exception {

		Professor professorExistente = professores.findByMatricula(matricula);

		if (professorExistente == null) {
			throw new Exception("Não existe Professor com essa matricula");
		}

		professorExistente.setNome(professor.getNome());
		professorExistente.setEmail(professor.getEmail());
		professorExistente.setDataNascimento(professor.getDataNascimento());
		professorExistente.setCpfCnpj(professor.getCpfCnpj());
		professorExistente.setTelefone(professor.getTelefone());
		professorExistente.setDepartamento(professor.getDepartamento());
		professorExistente.setEspecializacao(professor.getEspecializacao());
		professorExistente.setTitulacao(professor.getTitulacao());
		professorExistente.setSalario(professor.getSalario());
		professorExistente.setCargaHoraria(professor.getCargaHoraria());
		professorExistente.setDataAdmissao(professor.getDataAdmissao());

		Professor professorAtualizado = professores.save(professorExistente);

		return ResponseEntity.ok(professorAtualizado);
	}

	@DeleteMapping({ "/{matricula}", "/{matricula}/" })
	public ResponseEntity<Void> deletarProfessor(@PathVariable Integer matricula) throws Exception {
		Professor professor = professores.findByMatricula(matricula);

		if (professor == null) {
			throw new Exception("Não existe Professor com essa matricula");
		}

		professores.delete(professor);
		return ResponseEntity.noContent().build();
	}
}
