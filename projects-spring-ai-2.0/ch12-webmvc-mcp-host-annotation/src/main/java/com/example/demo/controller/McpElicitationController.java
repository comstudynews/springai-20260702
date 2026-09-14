package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.handler.McpElicitationHandler.PendingRequest;
import com.example.demo.service.McpElicitationService;

@RestController
@RequestMapping("/mcp-elicitation")
public class McpElicitationController {
  private McpElicitationService mcpElicitationService;

  public McpElicitationController(McpElicitationService mcpElicitationService) {
    this.mcpElicitationService = mcpElicitationService;
  }

  // 사용자의 질문을 처리하고 답변을 반환하는 엔드포인트
  @PostMapping(
	  value = "/chat",
	  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	  produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String chat(
	  @RequestParam("question") String question,
	  @RequestParam("taskId") String taskId) {
	return mcpElicitationService.chat(question, taskId);
  }

  // Elicitation 요청을 조회하는 엔드포인트
  @GetMapping(
	  value = "/find-pending-request",
	  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	  produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<PendingRequest> elicitationFindPendingRequest(
	  @RequestParam("taskId") String taskId) {
	PendingRequest request = mcpElicitationService.findPendingRequest(taskId);
	return request == null
		? ResponseEntity.noContent().build()
		: ResponseEntity.ok(request);
  }

  // Elicitation 요청을 승인하는 엔드포인트
  @PostMapping(
	  value = "/approve",
	  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	  produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Void> elicitationApprove(
	  @RequestParam("id") long id,
	  @RequestParam("taskId") String taskId) {
	return mcpElicitationService.respondToMcpServer(id, taskId, true)
		? ResponseEntity.noContent().build()
		: ResponseEntity.notFound().build();
  }

  // Elicitation 요청을 거부하는 엔드포인트
  @PostMapping(
	  value = "/decline",
	  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	  produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Void> elicitationDecline(
	  @RequestParam("id") long id,
	  @RequestParam("taskId") String taskId) {
	return mcpElicitationService.respondToMcpServer(id, taskId, false)
		? ResponseEntity.noContent().build()
		: ResponseEntity.notFound().build();
  }
}
