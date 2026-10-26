package com.example.demo.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Cliente HTTP (com timeouts) usado para chamar APIs externas. */
@Configuration
public class HttpClientConfig {

	@Bean
	public RestClient restClientExterno() {
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
		fabrica.setReadTimeout(Duration.ofSeconds(10));
		return RestClient.builder().requestFactory(fabrica).build();
	}
}
