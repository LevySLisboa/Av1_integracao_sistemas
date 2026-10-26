package com.example.demo.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RequisicaoInvalidaException;
import com.example.demo.exception.ServicoExternoException;
import com.example.demo.models.entity.Aluno;
import com.example.demo.models.entity.Prova;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Integração com a BrasilAPI (https://brasilapi.com.br), API pública externa:
 *  - /feriados/v1/{ano}  -> feriados nacionais
 *  - /cep/v2/{cep}       -> endereço a partir do CEP
 * Os dados externos são combinados com os dados locais (Prova e Aluno).
 */
@Service
public class IntegracaoService {

	private static final String BASE = "https://brasilapi.com.br/api";

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Feriado(String date, String name, String type) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Endereco(String cep, String state, String city, String neighborhood, String street, String service) {
	}

	private final RestClient http;

	public IntegracaoService(RestClient restClientExterno) {
		this.http = restClientExterno;
	}

	/** Requisição externa nº 1: feriados nacionais do ano. */
	public List<Feriado> feriados(int ano) {
		try {
			Feriado[] lista = http.get().uri(BASE + "/feriados/v1/{ano}", ano).retrieve().body(Feriado[].class);
			return lista == null ? List.of() : List.of(lista);
		} catch (RestClientException e) {
			throw new ServicoExternoException("Não foi possível consultar os feriados na BrasilAPI.", e);
		}
	}

	/** Requisição externa nº 2: endereço pelo CEP. */
	public Endereco enderecoPorCep(String cep) {
		try {
			Endereco e = http.get().uri(BASE + "/cep/v2/{cep}", cep).retrieve().body(Endereco.class);
			if (e == null) {
				throw new ServicoExternoException("Resposta vazia da BrasilAPI.");
			}
			return e;
		} catch (RestClientResponseException e) {
			HttpStatusCode status = e.getStatusCode();
			if (status.value() == 404) {
				throw new RecursoNaoEncontradoException("CEP não encontrado.");
			}
			throw new ServicoExternoException("A BrasilAPI retornou erro ao consultar o CEP.", e);
		} catch (RestClientException e) {
			throw new ServicoExternoException("Não foi possível consultar o CEP na BrasilAPI.", e);
		}
	}

	/** Dados locais da prova + feriados externos do ano da aplicação. */
	public Map<String, Object> calendarioDaProva(Prova prova) {
		LocalDate data = prova.getDataAplicacao();
		if (data == null) {
			throw new RequisicaoInvalidaException("Esta prova não possui dataAplicacao cadastrada.");
		}

		List<Feriado> feriados = feriados(data.getYear());
		Feriado coincidente = feriados.stream().filter(f -> data.toString().equals(f.date())).findFirst().orElse(null);
		List<Feriado> proximos = feriados.stream().filter(f -> f.date().compareTo(data.toString()) > 0).limit(3)
				.toList();
		boolean fimDeSemana = data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY;

		Map<String, Object> local = new LinkedHashMap<>();
		local.put("id", prova.getId());
		local.put("disciplina", prova.getDisciplina());
		local.put("assunto", prova.getAssunto());
		local.put("turma", prova.getTurma());
		local.put("av", prova.getAv());
		local.put("professor", prova.getProfessor().getNome());
		local.put("aluno", prova.getAluno().getNome());
		local.put("dataAplicacao", data.toString());
		local.put("diaDaSemana", data.getDayOfWeek().name());

		Map<String, Object> externo = new LinkedHashMap<>();
		externo.put("fonte", "BrasilAPI /feriados/v1/" + data.getYear());
		externo.put("coincideComFeriado", coincidente != null);
		externo.put("feriado", coincidente);
		externo.put("proximosFeriados", proximos);

		Map<String, Object> resposta = new LinkedHashMap<>();
		resposta.put("dadosLocais", local);
		resposta.put("dadosExternos", externo);
		resposta.put("alerta", coincidente != null ? "A prova está marcada em um feriado nacional: " + coincidente.name()
				: fimDeSemana ? "A prova está marcada em um fim de semana." : null);
		return resposta;
	}

	/** Dados locais do aluno + endereço externo obtido pelo CEP. */
	public Map<String, Object> localizacaoDoAluno(Aluno aluno, String cepInformado) {
		String cep = cepInformado == null ? "" : cepInformado.replaceAll("\\D", "");
		if (cep.length() != 8) {
			throw new RequisicaoInvalidaException("CEP inválido: informe 8 dígitos (ex.: 60811905).");
		}
		Endereco endereco = enderecoPorCep(cep);

		Map<String, Object> local = new LinkedHashMap<>();
		local.put("matricula", aluno.getMatricula());
		local.put("nome", aluno.getNome());
		local.put("curso", aluno.getCurso());
		local.put("semestre", aluno.getSemestreDeReferencia());

		Map<String, Object> resposta = new LinkedHashMap<>();
		resposta.put("dadosLocais", local);
		resposta.put("dadosExternos", Map.of("fonte", "BrasilAPI /cep/v2/" + cep, "endereco", endereco));
		resposta.put("resumo", aluno.getNome() + " — " + endereco.street() + ", " + endereco.neighborhood() + ", "
				+ endereco.city() + "/" + endereco.state());
		return resposta;
	}
}
