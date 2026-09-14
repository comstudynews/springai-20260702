package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class McpLoggingService {
  private ChatClient chatClient;

  public McpLoggingService(
    ChatClient.Builder chatClientBuilder, 
    ToolCallbackProvider toolCallbackProvider
  ) {
    this.chatClient = chatClientBuilder
      .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
      .defaultTools(toolCallbackProvider)
      .build();
  }

  public String chat(String question) {
    String answer = this.chatClient.prompt()
        .system("""
          현재 날짜와 시간 질문은 반드시 도구를 사용하세요.
        """)
        .user(question)
        .call()
        .content();
    return answer;
  }
}
