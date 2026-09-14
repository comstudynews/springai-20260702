package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

// MCP 서버에서 프롬프트와 자동완성을 조회하는 서비스
@Service
@Slf4j
public class McpPromptService {
  private final ChatClient chatClient;
  private final List<McpSyncClient> mcpSyncClients;

  public McpPromptService(
    ChatClient.Builder chatClientBuilder,
    ToolCallbackProvider toolCallbackProvider,
    List<McpSyncClient> mcpSyncClients) {
    this.chatClient = chatClientBuilder
      .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
      .defaultTools(toolCallbackProvider)
      .build();
    this.mcpSyncClients = mcpSyncClients;
  }

  // 연결된 모든 MCP Server가 제공하는 MCP 프롬프트 목록을 반환
  public Map<String, List<McpSchema.Prompt>> getListPrompts() {
    Map<String, List<McpSchema.Prompt>> promptsByServer = new LinkedHashMap<>();
    mcpSyncClients.forEach(client ->
        promptsByServer.put(client.getServerInfo().name(), client.listPrompts().prompts())
    );
    return promptsByServer;
  }

  // MCP Server가 제공하는 MCP 프롬프트를 조회
  public String getPrompt(String promptName, Map<String, Object> args) {
    McpSchema.GetPromptResult result = mcpSyncClients.get(0).getPrompt(
        McpSchema.GetPromptRequest.builder(promptName)
            .arguments(args)
            .build()
    );
    String prompt  = ((McpSchema.TextContent) result.messages().get(0).content()).text();
    return prompt;
  }

  // MCP 프롬프트 텍스트와 사용자 질문을 결합하여 LLM으로 요청하고 답변 생성
  public String springAiExplanation(String question, Map<String, Object> args) {
    String promptText = getPrompt("spring-ai-explanation-prompt", args);
    return chatClient.prompt()
        .user(promptText + "\n\n사용자 질문:\n" + question)
        .call()
        .content();
  }

  // MCP 프롬프트의 매개값에 대한 자동 완성 후보 목록을 조회
  public List<String> getListCompleteArgs(String promptName, String argName, String keyword) {
    McpSchema.CompleteResult result = mcpSyncClients.get(0).completeCompletion(
        McpSchema.CompleteRequest.builder(
            new McpSchema.PromptReference(promptName),
            new McpSchema.CompleteRequest.CompleteArgument(
              argName, keyword != null ? keyword : "")
        ).build()
    );
    return result.completion().values();
  }

  // 차량 출입 제어 프롬프트 텍스트와 이미지 데이터를 결합하여 LLM으로 요청하고 
  // 장치 제어 결과 답변을 받아 반환
public String vehicleAccessControl(String contentType, byte[] bytes, String device) {
  // MCP 프롬프트 텍스트를 가져옴
  String promptText = getPrompt(
    "vehicle-access-control-prompt", 
    Map.of("device", device));

  // 이미지 데이터를 Media 객체로 생성
  Media media = Media.builder()
      .mimeType(MimeType.valueOf(contentType))
      .data(new ByteArrayResource(bytes))
      .build();

  // 프롬프트 텍스트와 이미지를 하나의 사용자 메시지로 결합
  UserMessage userMessage = UserMessage.builder()
      .text(promptText)
      .media(media)
      .build();

  // LLM 호출
  return chatClient.prompt()
      .messages(userMessage)
      .call()
      .content();
}  

  // 지정한 언어로 답변하는 프롬프트와 사용자 질문을 결합하여 LLM으로 요청
  public String multilingualAnswer(String question, Map<String, Object> args) {
    String promptText = getPrompt("multilingual-answer-prompt", args);
    return chatClient.prompt()
        .user(promptText + "\n\n사용자 질문:\n" + question)
        .call()
        .content();
  }  
}
