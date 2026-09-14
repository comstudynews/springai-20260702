package com.example.demo.toolsearch;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.eviction.LruEvictionStrategy;
import org.springframework.ai.tool.toolsearch.index.lucene.LuceneToolIndex;
import org.springframework.stereotype.Service;

import com.example.demo.datetime.DateTimeTools;
import com.example.demo.recommendmovie.RecommendMovieTools;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ToolSearchService {
  // ##### 필드 #####
  private ChatClient chatClient;

  // ##### 생성자 #####
  public ToolSearchService(ChatClient.Builder chatClientBuilder,
                            DateTimeTools dateTimeTools,
                            RecommendMovieTools recommendMovieTools) {
    this.chatClient = chatClientBuilder
        .defaultTools(dateTimeTools, recommendMovieTools)
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .build();
  }

  // ##### LLM과 대화하는 메소드 #####
  public String chat(String question, String sessionId) {
    // HttpCaptureLogger.isPrint = true;

    String answer = this.chatClient.prompt()
        .system("""
            현재 날짜와 현재 시간에 대한 답변은 반드시 도구를 사용하세요.
            영화와 관련된 질문은 반드시 도구 호출을 사용하세요.
            도구 호출 전에 사용자에게 재확인하는 답변을 생성하지 마세요. 
        """)
        .user(question)
        .advisors(advisorSpec -> advisorSpec
            .param(ChatMemory.CONVERSATION_ID, sessionId)
            .param("tool-search-id", sessionId)
        )
        .call()
        .content();
    
    // HttpCaptureLogger.isPrint = false;
    return answer;
  }
}