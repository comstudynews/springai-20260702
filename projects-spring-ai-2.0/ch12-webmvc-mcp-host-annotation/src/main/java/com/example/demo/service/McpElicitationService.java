package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import com.example.demo.handler.McpElicitationHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class McpElicitationService {
  private ChatClient chatClient;
  private McpElicitationHandler elicitationHandler;


  public McpElicitationService(
    ChatClient.Builder chatClientBuilder, 
    ToolCallbackProvider toolCallbackProvider,
    McpElicitationHandler elicitationHandler
  ) {
    this.chatClient = chatClientBuilder
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .defaultTools(toolCallbackProvider)
        .build();
    this.elicitationHandler = elicitationHandler;
  }

  public String chat(String question, String taskId) {
    log.info("taskId: {}", taskId);
    String answer = this.chatClient.prompt()
        .system("""
            파일과 디렉토리 관련 요청은 반드시 도구를 사용하세요.
            파일 삭제 결과는 JSON, 배열, 코드 블록으로 감싸지 말고 결과 문장만 반환하세요.
            삭제 도구 실행 후 사용자에게 다시 승인 여부를 질문하지 마세요.
            """)
        .user(question)
        .toolContext(Map.of("taskId", taskId))
        .call()
        .content();
    return answer;
  }

  public McpElicitationHandler.PendingRequest findPendingRequest(String taskId) {
    return elicitationHandler.findPendingRequest(taskId);
  }

  public boolean respondToMcpServer(long id, String taskId, boolean approved) {
    return elicitationHandler.respondToMcpServer(id, taskId, approved);
  }
}
