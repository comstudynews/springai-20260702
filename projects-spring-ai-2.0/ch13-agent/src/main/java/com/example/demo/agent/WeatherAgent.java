package com.example.demo.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WeatherAgent {
  // 에이전트의 역할과 행동 범위를 정의하는 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
    당신은 날씨 정보를 제공하는 전문 에이전트입니다.
    날씨 정보가 필요하면 반드시 Tool을 사용해 조회하세요.
    추측으로 답변하지 마세요.
  """;

  private final ChatClient chatClient;

  // ChatClient 초기화하는 생성자
  public WeatherAgent(
        ChatClient.Builder builder,
        ChatMemory chatMemory) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .build();
  }

  // 에이전트를 실행하는 메서드
  public String execute(String conversationId, String userQuery) {
    return chatClient.prompt()
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .user(userQuery)
        .tools(this)
        .call()
        .content();
  }

  // 도구 정의
  @Tool(description = "제공된 도시의 현재 날씨 정보를 조회합니다")
  public String getWeather(@ToolParam(description = "도시 이름") String city) {
    // 실제 API 호출 대신 예시 응답을 반환하도록 구현
    return String.format("%s의 현재 날씨는 맑고 23도입니다.", city);
  }  
}

