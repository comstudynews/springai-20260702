package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class McpToolChangedService {
  private ChatClient chatClient;
  private List<McpSyncClient> mcpSyncClients;
  private RestClient restClient;

  public McpToolChangedService(
    ChatClient.Builder chatClientBuilder, 
    ToolCallbackProvider toolCallbackProvider,
    List<McpSyncClient> mcpSyncClients,
    RestClient.Builder restClientBuilder
  ) {
    this.chatClient = chatClientBuilder
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .defaultTools(toolCallbackProvider)
        .build();
    this.mcpSyncClients = mcpSyncClients;
    this.restClient = restClientBuilder.baseUrl("http://localhost:8081").build();
  }

  // 연결된 모든 MCP Server가 제공하는 도구 목록을 반환
  public Map<String, List<McpSchema.Tool>> getListTools() {
    // MCP Server별로 제공하는 도구 목록을 저장할 맵 생성
    Map<String, List<McpSchema.Tool>> toolsByServer = new LinkedHashMap<>();
    // 각 MCP Server별로 제공하는 도구 목록을 조회하여 맵에 저장
    mcpSyncClients.forEach(client ->
        toolsByServer.put(client.getServerInfo().name(), client.listTools().tools())
    );
    // 맵 반환
    return toolsByServer;
  }
  
  // MCP Server에 동적 도구 생성
  public String addTool() {
    return restClient.get()
        .uri("/tool/add")
        .retrieve()
        .body(String.class);
  }

  // MCP Server에서 동적 도구 제거
  public String removeTool() {
    return restClient.get()
        .uri("/tool/remove")
        .retrieve()
        .body(String.class);
  }

  // 사용자의 질문에 대해 연결된 MCP Server의 도구를 활용하여 답변을 생성
  public String chat(String question) {
    return this.chatClient.prompt()
        .user(question)
        .call()
        .content();
  }
}
