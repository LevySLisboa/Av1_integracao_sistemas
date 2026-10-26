package com.example.demo.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Rate limiter de janela fixa, em memória, por chave (ex.: IP). Simples de
 * explicar e de testar. Limitação: o estado não é compartilhado entre várias
 * instâncias da aplicação (ver README).
 */
public class LimitadorDeRequisicoes {

	private static final int LIMITE_PARA_LIMPEZA = 10_000;

	private static final class Janela {
		volatile long inicio;
		int contagem;

		Janela(long inicio) {
			this.inicio = inicio;
		}
	}

	private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
	private final int maximo;
	private final long janelaMs;
	private final LongSupplier relogio;

	public LimitadorDeRequisicoes(int maximo, long janelaMs, LongSupplier relogio) {
		this.maximo = maximo;
		this.janelaMs = janelaMs;
		this.relogio = relogio;
	}

	/** Registra uma requisição e informa se ela está dentro do limite. */
	public boolean permitir(String chave) {
		long agora = relogio.getAsLong();
		if (janelas.size() > LIMITE_PARA_LIMPEZA) {
			janelas.entrySet().removeIf(e -> agora - e.getValue().inicio >= janelaMs);
		}
		Janela j = janelas.computeIfAbsent(chave, k -> new Janela(agora));
		synchronized (j) {
			if (agora - j.inicio >= janelaMs) {
				j.inicio = agora;
				j.contagem = 0;
			}
			j.contagem++;
			return j.contagem <= maximo;
		}
	}

	/** Segundos até a janela da chave reiniciar (mínimo 1). */
	public long segundosParaLiberar(String chave) {
		Janela j = janelas.get(chave);
		if (j == null) {
			return 1;
		}
		long restante = janelaMs - (relogio.getAsLong() - j.inicio);
		return Math.max(1, (restante + 999) / 1000);
	}
}
