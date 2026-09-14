package com.example.demo.handler;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.Tool;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpToolListChangedHandler {

  @McpToolListChanged(clients = "tool-server")
  public void handleToolListChanged(List<Tool> updatedTools) {
    log.info("MCP 도구 목록이 변경되었습니다. 도구 수: {}", updatedTools.size());
    updatedTools.forEach(tool ->
      log.info("도구 이름: {}, 설명: {}", tool.name(), tool.description())
    );
  }
}