package com.example.demo.internetsearch;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InternetSearchService {
  // ##### 필드 #####
  private ChatClient chatClient;

  @Autowired
  private InternetSearchTools internetSearchTools;

  // ##### 생성자 #####
  public InternetSearchService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder
      .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
      .build();
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chat(String question) {
    String answer = this.chatClient.prompt()
        .system("""
          사용자의 질문에 대해 추가 질문 없이 즉시 도구를 사용하여 검색하고 
          결과를 한국어로 답변하세요.
        """)
        .user(question)
        .tools(internetSearchTools)
        .call()
        .content();
    return answer;
  }
}
