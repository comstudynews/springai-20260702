package com.example.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.McpPromptService;

import io.modelcontextprotocol.spec.McpSchema;

@RestController
@RequestMapping("/mcp-prompt")
public class McpPromptController {
  private McpPromptService mcpPromptService;

  public McpPromptController(McpPromptService mcpPromptService) {
    this.mcpPromptService = mcpPromptService;
  }

  // MCP Server가 제공하는 프롬프트 목록을 조회
  @GetMapping(
    value = "/list-prompts", 
    produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, List<McpSchema.Prompt>> listPrompts() {
    return mcpPromptService.getListPrompts();
  }

  // MCP Server가 제공하는 프롬프트를 조회
  @PostMapping(
    value = "/get-prompt", 
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
    produces = MediaType.TEXT_PLAIN_VALUE)
  public String getPrompt(
      @RequestParam("promptName") String promptName,
      @RequestParam Map<String, String> requestParams) {
    Map<String, Object> args = new HashMap<>(requestParams);
    args.remove("promptName");
    return mcpPromptService.getPrompt(promptName, args);
  }  

  // MCP Server가 제공하는 프롬프트와 텍스트 질문을 결합하여 LLM으로 요청하고 답변 생성
  @PostMapping(
    value = "/spring-ai-explanation-prompt",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE)
  public String springAiExplanationPrompt(
      @RequestParam("promptName") String promptName,
      @RequestParam("question") String question,
      @RequestParam Map<String, String> requestParams) {
    Map<String, Object> args = new HashMap<>(requestParams);
    // requestParams에는 promptName과 question도 포함되어 있으므로, 
    // args에서 제거하여 MCP 프롬프트 매개값으로 전달하지 않도록 해야함
    args.remove("promptName");
    args.remove("question");
    return mcpPromptService.springAiExplanation(question, args);
  }

  // MCP Server가 제공하는 프롬프트의 매개값 후보 목록을 조회
  @GetMapping(
    value = "/list-complete-arg", 
    produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> listCompleteArg(
      @RequestParam("promptName") String promptName,
      @RequestParam("argName") String argName,
      @RequestParam("keyword") String keyword) {
    return mcpPromptService.getListCompleteArgs(promptName, argName, keyword);
  }  

  // MCP Server가 제공하는 프롬프트와 첨부 파일을 결합하여 LLM으로 요청하고 답변 생성
  @PostMapping(
    value = "/vehicle-access-control-prompt", 
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
    produces = MediaType.TEXT_PLAIN_VALUE)
  public String vehicleAccessControlPrompt(
      @RequestParam("attach") MultipartFile attach,
      @RequestParam("promptName") String promptName,
      @RequestParam("device") String device) throws IOException {
    return mcpPromptService.vehicleAccessControl(
        attach.getContentType(), attach.getBytes(), device);
  }

  // 지정한 언어로 사용자 질문에 답변 생성
  @PostMapping(
    value = "/multilingual-answer-prompt",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE)
  public String multilingualAnswerPrompt(
      @RequestParam("promptName") String promptName,
      @RequestParam("question") String question,
      @RequestParam Map<String, String> requestParams) {
    Map<String, Object> args = new HashMap<>(requestParams);
    args.remove("promptName");
    args.remove("question");
    return mcpPromptService.multilingualAnswer(question, args);
  }
}
