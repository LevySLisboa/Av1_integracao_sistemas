package com.example.demo.models.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.entity.Aluno;

@Repository
public interface Alunos extends JpaRepository<Aluno, Long> {

	Aluno findByMatricula(Integer matricula);
}
