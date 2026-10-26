package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class LimitadorDeRequisicoesTest {

	@Test
	void bloqueiaAposExcederOLimiteDentroDaJanela() {
		AtomicLong relogio = new AtomicLong(0);
		LimitadorDeRequisicoes l = new LimitadorDeRequisicoes(3, 60_000, relogio::get);

		assertTrue(l.permitir("1.1.1.1"));
		assertTrue(l.permitir("1.1.1.1"));
		assertTrue(l.permitir("1.1.1.1"));
		assertFalse(l.permitir("1.1.1.1"));
	}

	@Test
	void chavesDiferentesTemContadoresIndependentes() {
		LimitadorDeRequisicoes l = new LimitadorDeRequisicoes(1, 60_000, () -> 0L);

		assertTrue(l.permitir("a"));
		assertFalse(l.permitir("a"));
		assertTrue(l.permitir("b"));
	}

	@Test
	void liberaNovamenteDepoisQueAJanelaExpira() {
		AtomicLong relogio = new AtomicLong(0);
		LimitadorDeRequisicoes l = new LimitadorDeRequisicoes(1, 60_000, relogio::get);

		assertTrue(l.permitir("ip"));
		assertFalse(l.permitir("ip"));
		relogio.set(60_001);
		assertTrue(l.permitir("ip"));
	}

	@Test
	void informaSegundosParaLiberar() {
		AtomicLong relogio = new AtomicLong(0);
		LimitadorDeRequisicoes l = new LimitadorDeRequisicoes(1, 60_000, relogio::get);

		l.permitir("ip");
		relogio.set(30_000);
		assertEquals(30, l.segundosParaLiberar("ip"));
	}
}
