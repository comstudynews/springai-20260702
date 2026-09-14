package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.McpToolChangedService;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/mcp-tool-changed")
@Slf4j
public class McpToolChangedController {
  private McpToolChangedService mcpToolChangedService;

  public McpToolChangedController(McpToolChangedService mcpToolChangedService) {
    this.mcpToolChangedService = mcpToolChangedService;
  }

  @PostMapping(
    value = "/chat",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String chat(@RequestParam("question") String question) {
    return mcpToolChangedService.chat(question);
  }

  @GetMapping(
    value = "/list-tools",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Map<String, List<McpSchema.Tool>> getToolList() {
    return mcpToolChangedService.getListTools();
  }

  @GetMapping(
    value = "/add-tool",
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String addTool() {
    return mcpToolChangedService.addTool();
  }

  @GetMapping(
    value = "/remove-tool",
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String removeTool() {
    return mcpToolChangedService.removeTool();
  }
}
