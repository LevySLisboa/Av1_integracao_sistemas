package com.example.demo.models.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.entity.Prova;

@Repository
public interface Provas extends JpaRepository<Prova, Long> {

}