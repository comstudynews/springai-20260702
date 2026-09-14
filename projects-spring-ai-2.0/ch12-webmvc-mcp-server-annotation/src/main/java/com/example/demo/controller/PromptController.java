package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.provider.PromptProvider;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

@RestController
@RequestMapping("/prompt")
public class PromptController {
  private final String promptName = "multilingual-answer-prompt";

  private final ObjectProvider<McpSyncServer> mcpSyncServerProvider;
  private final PromptProvider promptProvider;

  public PromptController(
      ObjectProvider<McpSyncServer> mcpSyncServerProvider,
      PromptProvider promptProvider) {
    this.mcpSyncServerProvider = mcpSyncServerProvider;
    this.promptProvider = promptProvider;
  }

	// 실행 중인 MCP Server에 프롬프트를 추가
	@GetMapping("/add")
  public String addPrompt() {
    // 프롬프트 기본 정보 정의
    McpSchema.Prompt prompt = McpSchema.Prompt.builder(promptName)
        .description("사용자의 질문에 지정한 언어로 답변하도록 안내하는 동적 프롬프트")
        .arguments(List.of(
            McpSchema.PromptArgument.builder("language")
                .description("답변에 사용할 언어")
                .required(true)
                .build()))
        .build();

    // 매개값을 받아 다국어 답변 프롬프트를 작성하는 프롬프트 명세 작성
    McpServerFeatures.SyncPromptSpecification promptSpecification =
        new McpServerFeatures.SyncPromptSpecification(
          prompt,
          (exchange, request) -> {
            String language = String.valueOf(request.arguments().get("language"));
            String promptText = promptProvider.getMultilingualAnswerPrompt(language);
            McpSchema.PromptMessage message = McpSchema.PromptMessage.builder(
              McpSchema.Role.USER,
              McpSchema.TextContent.builder(promptText).build()
            ).build();
            return McpSchema.GetPromptResult.builder(List.of(message)).build();
        });
    
    // 실행 중인 MCP Server에 프롬프트 명세를 추가
    mcpSyncServerProvider.getObject().addPrompt(promptSpecification);
    return "MCP 프롬프트를 추가했습니다: " + promptName;
  }

	// 실행 중인 MCP Server에서 프롬프트를 제거
  @GetMapping("/remove")
  public String removePrompt() {
    mcpSyncServerProvider.getObject().removePrompt(promptName);
    return "MCP 프롬프트를 제거했습니다: " + promptName;
  }
}
