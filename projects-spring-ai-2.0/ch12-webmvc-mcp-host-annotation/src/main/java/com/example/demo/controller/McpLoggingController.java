package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.McpLoggingService;

@RestController
@RequestMapping("/mcp-logging")
public class McpLoggingController {
  private McpLoggingService mcpLoggingService;

  public McpLoggingController(McpLoggingService mcpLoggingService) {
    this.mcpLoggingService = mcpLoggingService;
  }

  // 사용자의 질문을 처리하고 답변을 반환하는 엔드포인트
  @PostMapping(
      value = "/chat",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String chat(@RequestParam("question") String question) {
    return mcpLoggingService.chat(question);
  }
}