package com.example.demo.models.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.models.entity.Professor;

public interface Professores extends JpaRepository<Professor, Long> {

	Professor findByMatricula(Integer matricula);

}
