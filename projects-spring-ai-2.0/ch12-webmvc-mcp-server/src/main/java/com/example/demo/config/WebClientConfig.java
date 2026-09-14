package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	// implementation 'org.springframework.ai:spring-ai-starter-model-openai'가 
	// 의존성으로 추가되어 있지 않기 때문에. WebClient.Builder가 빈으로 자동 생성되지 않음
	// 그래서 수동으로 빈을 생성
  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
