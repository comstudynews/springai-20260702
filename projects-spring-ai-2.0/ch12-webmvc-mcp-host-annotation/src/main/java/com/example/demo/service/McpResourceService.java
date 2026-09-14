package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class McpResourceService {
  private ChatClient chatClient;

  // 연결된 MCP Server별로 통신하는 McpSyncClient 목록
  private List<McpSyncClient> mcpSyncClients;

  public McpResourceService(
    ChatClient.Builder chatClientBuilder, 
    ToolCallbackProvider toolCallbackProvider,
    List<McpSyncClient> mcpSyncClients) {
    this.chatClient = chatClientBuilder
      .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
      .defaultTools(toolCallbackProvider)
      .build();
    this.mcpSyncClients = mcpSyncClients;
  }

  // 연결된 MCP Server별로 제공하는 리소스 정보를 맵으로 반환
  public Map<String, List<ResourceInfo>> getListResources() {
    // 서버별로 제공하는 리소스 정보를 저장할 맵 생성
    Map<String, List<ResourceInfo>> resourcesByServer = new LinkedHashMap<>();
    mcpSyncClients.forEach(client -> {
      // 리소스 정보를 저장하는 리스트 생성
      List<ResourceInfo> resources = new java.util.ArrayList<>();
      // 리소스를 ResourceInfo 객체로 변환하고 리스트에 추가
      client.listResources().resources().forEach(resource -> resources.add(
        new ResourceInfo(resource.name(), 
                        resource.uri(), 
                        resource.description(),
                        resource.mimeType(), 
                        "리소스")
      ));
      // 리소스 템플릿을 ResourceInfo 객체로 변환하고 리스트에 추가
      client.listResourceTemplates().resourceTemplates().forEach(resourceTemplate -> resources.add(
        new ResourceInfo(resourceTemplate.name(), 
                        resourceTemplate.uriTemplate(), 
                        resourceTemplate.description(), 
                        resourceTemplate.mimeType(), 
                        "리소스템플릿")
      ));
      // 서버 연결 이름을 키로 ResourceInfo 목록을 맵에 저장
      resourcesByServer.put(client.getServerInfo().name(), resources);
    });
    return resourcesByServer;
  } 
  
  // 리소스 정보를 담고 있는 레코드 정의
  public record ResourceInfo(String name, String uri, String description, String mimeType, String type) {
  }

  // MCP Server가 제공하는 리소스 가져오기
  public String getResource(String uri) {
    // URI로 리소스 요청 전송
    McpSchema.ReadResourceResult result = mcpSyncClients.get(0).readResource(
        McpSchema.ReadResourceRequest.builder(uri).build()
    );
    // 응답 텍스트 추출
    String resource = ((McpSchema.TextResourceContents) result.contents().get(0)).text();
    return resource;
  }

  // 리소스 템플릿의 매개값으로 사용될 자동 완성된 후보 문자열 목록 조회; null이면 전체 목록 반환
  public List<String> getListResourceTemplateArgs(String uri, String argName, String keyword) {
    McpSchema.CompleteResult result = mcpSyncClients.get(0).completeCompletion(
        McpSchema.CompleteRequest.builder(
            // 리소스 템플릿 URI
            new McpSchema.ResourceReference(uri),
            // 매개값 지정
            new McpSchema.CompleteRequest.CompleteArgument(argName, keyword != null ? keyword : "")
        ).build()
    );
    // 자동 완성된 후보 문자열 목록 반환
    return result.completion().values();
  }  

  // 리소스 기반의 답변 생성
  public String chat(String uri, String question) {
    String resource = getResource(uri);
    return chatClient.prompt()
        .system("다음 리소스를 기반으로 답변하세요:\n" + resource)
        .user(question)
        .call()
        .content();
  }  
}
