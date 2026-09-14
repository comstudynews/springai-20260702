package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import com.openai.models.ReasoningEffort;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiServiceByChatClient {
	// ##### 필드 #####
	private ChatClient chatClient;

	// ##### 생성자 #####
	public AiServiceByChatClient(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	// ##### 메소드 #####
	public String generateText(String question) {
		long startTime = System.currentTimeMillis();
		String answer = chatClient.prompt()
				.system("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
				.user(question)
				.options(
						// Spring AI 2.0.0에서는 OpenAiChatOptions를 사용하여 ChatOptions를 생성
						OpenAiChatOptions.builder()
								// gpt-5-mini는 무조건 1.0임, 따라서 temperature 설정 불필요
								.temperature(1.0)
								// gpt-5-mini는 maxTokens 대신 maxCompletionTokens(추론 토큰 포함) 사용
								.maxCompletionTokens(5000)
								// gpt-5 모델의 추론 노력 수준: minimal, low, medium(기본), high 중 하나로 설정
								.reasoningEffort("low")								
								// Spring AI 2.0.0은 Builder 객체를 매개값으로 제공해야 됨
								// .build() 
				)
				.call()
				.content();
		long endTime = System.currentTimeMillis();
		log.info("generateText() 수행 시간: {} ms", (endTime - startTime));
		return answer;
	}

	public Flux<String> generateStreamText(String question) {
		Flux<String> fluxString = chatClient.prompt()
				.system("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
				.user(question)
				.options(OpenAiChatOptions.builder()
								   .maxCompletionTokens(5000)
								   .reasoningEffort("low"))
				.stream()
				.content();
		return fluxString;
	}
}
