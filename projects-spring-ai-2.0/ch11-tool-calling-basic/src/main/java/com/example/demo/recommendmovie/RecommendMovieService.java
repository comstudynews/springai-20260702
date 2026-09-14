package com.example.demo.recommendmovie;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecommendMovieService {
  // ##### 필드 #####
  private ChatClient chatClient;

  @Autowired
  private RecommendMovieTools recommendMovieTools;

  // ##### 생성자 #####
  public RecommendMovieService(ChatModel chatModel) {
    this.chatClient = ChatClient.builder(chatModel).build();
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chat(String question) {
    String answer = chatClient.prompt()
    			.system("당신은 영화 추천 전문가입니다. 반드시 도구를 이용해서 답변을 하세요")
        .user(question)
        .tools(recommendMovieTools)
        .call()
        .content();
    return answer;
  }
}
