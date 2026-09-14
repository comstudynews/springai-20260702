package com.example.demo.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {
	// ##### 필드 #####
	@Autowired
	private ChatModel chatModel;

	// ##### 메소드 #####
	public String generateText(String question) {
		// 시스템 메시지 생성
		SystemMessage systemMessage = SystemMessage.builder()
				.text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
				.build();

		// 사용자 메시지 생성
		UserMessage userMessage = UserMessage.builder()
				.text(question)
				.build();

		// 대화 옵션 설정 
		// Spring AI 2.0.0에서는 OpenAiChatOptions를 사용하여 ChatOptions를 생성
		ChatOptions chatOptions = OpenAiChatOptions.builder()
				// 런타임시 LLM 변경
				.model("gpt-5-mini")				
				// gpt-5-mini는 무조건 1.0이므로 설정 불필요
				.temperature(1.0)				
				// gpt-5-mini는 maxTokens 대신 maxCompletionTokens(추론 토큰 포함) 사용
				.maxCompletionTokens(5000) 
				// gpt-5 모델의 추론 노력 수준: minimal, low, medium(기본), high 중 하나로 설정
				.reasoningEffort("low")
				// ChatOptions 생성
				.build();
		
		// 프롬프트 생성 
		Prompt prompt = Prompt.builder()
				.messages(systemMessage, userMessage)
				.chatOptions(chatOptions)
				.build();
		
		// LLM에게 요청하고 응답받기 
		long startTime = System.currentTimeMillis();
		ChatResponse chatResponse = chatModel.call(prompt);
		long endTime = System.currentTimeMillis();
		log.info("LLM 응답 시간: {} ms", (endTime - startTime));
		AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
		String answer = assistantMessage.getText();
		return answer;

		/*
		// 여러개의 응답 후보를 얻고자 할 경우
		// Spring AI 2.0.0에서는 OpenAiChatOptions로 생성
		ChatOptions chatOptions = OpenAiChatOptions.builder()
				.n(3)
				.build();

		// 프롬프트 생성
		Prompt prompt = Prompt.builder()
				.messages(systemMessage, userMessage)
				.chatOptions(chatOptions)
				.build();

		ChatResponse chatResponse = chatModel.call(prompt);
		for (Generation generation : chatResponse.getResults()) {
			log.info("응답 후보: {}", generation.getOutput().getText());
		}
		return chatResponse.getResults().get(0).getOutput().getText();
		*/
	}

	public Flux<String> generateStreamText(String question) {
		// 시스템 메시지 생성
		SystemMessage systemMessage = SystemMessage.builder()
				.text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
				.build();

		// 사용자 메시지 생성
		UserMessage userMessage = UserMessage.builder()
				.text(question)
				.build();

		// 대화 옵션 설정
		ChatOptions chatOptions = OpenAiChatOptions.builder()			
				.maxCompletionTokens(5000) 		
				.reasoningEffort("low")
				.build();

		// 프롬프트 생성
		Prompt prompt = Prompt.builder()
				.messages(systemMessage, userMessage)
				.chatOptions(chatOptions)
				.build();

		// LLM에게 요청하고 응답받기
		Flux<ChatResponse> fluxResponse = chatModel.stream(prompt);
		Flux<String> fluxString = fluxResponse.map(chatResponse -> {
			if(chatResponse.getResult() != null) {
				AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
				String chunk = assistantMessage.getText();
				if(chunk == null) chunk = "";
				return chunk;
			} else {
				return "";
			}
		});

		return fluxString;
	}
}
