package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MascaraTest {

	@Test
	void mascaraEmailNormal() {
		assertEquals("j***@exemplo.com", Mascara.email("joao@exemplo.com"));
	}

	@Test
	void naoQuebraComEntradaInvalida() {
		assertEquals("***", Mascara.email("semarroba"));
		assertEquals("***", Mascara.email(null));
		assertEquals("***@x.com", Mascara.email("@x.com"));
	}
}
