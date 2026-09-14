package com.example.demo.handler;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpResourceListChanged;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpResourceListChangedHandler {
  @McpResourceListChanged(clients = "tool-server")
  public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
    log.info("MCP 리소스 목록이 변경되었습니다. 리소스 수: {}", updatedResources.size());
    updatedResources.forEach(resource ->
      log.info("리소스 이름: {}, 설명: {}", resource.name(), resource.description())
    );
  }
}