package com.example.demo.controllers;

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

import com.example.demo.models.entity.Prova;
import com.example.demo.models.repository.Provas;

@RestController
@RequestMapping("/provas")
public class ProvaController {

	@Autowired
	private Provas provas;

	@GetMapping({ "", "/" })
	public ResponseEntity<List<Prova>> listar() {
		return ResponseEntity.ok(provas.findAll());
	}

	@GetMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Prova> buscarPorId(@PathVariable Long id) throws Exception {
		Prova prova = provas.findById(id).orElse(null);

		if (prova == null) {
			throw new Exception("Não existe Prova com esse id");
		}

		return ResponseEntity.ok(prova);
	}

	@PostMapping({ "", "/" })
	public ResponseEntity<Prova> criarProva(@RequestBody Prova prova) throws Exception {
		if (prova == null) {
			throw new Exception("Não foi possível criar uma prova com esses dados");
		}

		return ResponseEntity.ok(provas.save(prova));
	}

	@PutMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Prova> editarProva(@PathVariable Long id, @RequestBody Prova prova) throws Exception {
		Prova provaExistente = provas.findById(id).orElse(null);

		if (provaExistente == null) {
			throw new Exception("Não existe Prova com esse id");
		}

		provaExistente.setDisciplina(prova.getDisciplina());
		provaExistente.setNota(prova.getNota());
		provaExistente.setAv(prova.getAv());
		provaExistente.setAssunto(prova.getAssunto());
		provaExistente.setProfessor(prova.getProfessor());
		provaExistente.setTurma(prova.getTurma());
		provaExistente.setAluno(prova.getAluno());

		return ResponseEntity.ok(provas.save(provaExistente));
	}

	@DeleteMapping({ "/{id}", "/{id}/" })
	public ResponseEntity<Void> deletarProva(@PathVariable Long id) throws Exception {
		Prova prova = provas.findById(id).orElse(null);

		if (prova == null) {
			throw new Exception("Não existe Prova com esse id");
		}

		provas.delete(prova);
		return ResponseEntity.noContent().build();
	}
}