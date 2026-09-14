package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Hotel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceNativeStructuredOutput {
	// ##### 필드 #####
	private ChatClient chatClient;

	// ##### 생성자 #####
	public AiServiceNativeStructuredOutput(ChatClient.Builder chatClientBuilder) {
		chatClient = chatClientBuilder.build();
	}

	// ##### 메소드 #####
	public Hotel getBean1(String city) {
		Hotel hotel = chatClient.prompt()
				.advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
				.user("%s에서 유명한 호텔 목록 5개를 출력하세요.".formatted(city))
				.call()
				.entity(Hotel.class,
								spec -> spec.validateSchema());
		return hotel;
	}

	public Hotel getBean2(String city) {
		log.info("실행");
		Hotel hotel = chatClient.prompt()
				.user("%s에서 유명한 호텔 목록 5개를 출력하세요.".formatted(city))
				.call()
				.entity(Hotel.class,
								spec -> spec
									.useProviderStructuredOutput()
									.validateSchema());
		return hotel;
	}
}
