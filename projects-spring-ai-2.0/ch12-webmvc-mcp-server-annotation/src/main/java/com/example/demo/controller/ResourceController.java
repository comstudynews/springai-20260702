package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.provider.ResourceProvider;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

@RestController
@RequestMapping("/resource")
public class ResourceController {
  private final ObjectProvider<McpSyncServer> mcpSyncServerProvider;
  private final ResourceProvider resourceProvider;

  public ResourceController(
      ObjectProvider<McpSyncServer> mcpSyncServerProvider,
      ResourceProvider resourceProvider) {
    this.mcpSyncServerProvider = mcpSyncServerProvider;
    this.resourceProvider = resourceProvider;
  }

  // 실행 중인 MCP Server에 리소스를 추가
  @GetMapping("/add")
  public String addResource() {
    McpSchema.Resource resource = McpSchema.Resource
        .builder(
            "manual://material-management",
            "자재 관리 매뉴얼")
        .description("자재의 입고, 보관, 출고 및 재고 점검 절차를 제공합니다.")
        .mimeType("text/plain")
        .build();

    McpServerFeatures.SyncResourceSpecification resourceSpecification = 
      new McpServerFeatures.SyncResourceSpecification(
        resource,
        (exchange, request) -> McpSchema.ReadResourceResult.builder(
            List.of(
                McpSchema.TextResourceContents.builder(
                    request.uri(),
                    resourceProvider.getMaterialManagementManual())
                    .build()))
            .build());

    mcpSyncServerProvider.getObject().addResource(resourceSpecification);
    return "MCP 리소스를 추가했습니다: " + resource.uri();
  }

  // 실행 중인 MCP Server에서 리소스를 제거
  @GetMapping("/remove")
  public String removeResource() {
    String resourceUri = "manual://material-management";
    mcpSyncServerProvider.getObject().removeResource(resourceUri);
    return "MCP 리소스를 제거했습니다: " + resourceUri;
  }
}