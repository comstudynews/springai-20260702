package com.example.demo.controller;

import java.lang.reflect.Method;
import java.util.Objects;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.tool.DynamicTools;

import io.modelcontextprotocol.server.McpSyncServer;

@RestController
@RequestMapping("/tool")
public class ToolController {
  private final ObjectProvider<McpSyncServer> mcpSyncServerProvider;
  private final DynamicTools dynamicTools;

  public ToolController(
    ObjectProvider<McpSyncServer> mcpSyncServerProvider,
    DynamicTools dynamicTools
  ) {
    this.mcpSyncServerProvider = mcpSyncServerProvider;
    this.dynamicTools = dynamicTools;
  }

  // 실행 중인 MCP Server에 도구를 추가
  @GetMapping("/add")
  public String addTool() {
    Method toolMethod = Objects.requireNonNull(
        ReflectionUtils.findMethod(
          DynamicTools.class, 
          "getMemberInfo", 
          String.class)
    );

    ToolCallback toolCallback = MethodToolCallback.builder()
        .toolDefinition(ToolDefinitions.builder(toolMethod)
            .description("회원 ID에 해당하는 회원 정보를 반환합니다.")
            .build())
        .toolMethod(toolMethod)
        .toolObject(dynamicTools)
        .build();

    mcpSyncServerProvider.getObject().addTool(McpToolUtils.toSyncToolSpecification(toolCallback));
    return "MCP 도구를 추가했습니다: " + "getMemberInfo";
  }

  // 실행 중인 MCP Server에서 도구를 제거
  @GetMapping("/remove")
  public String removeTool() {
    mcpSyncServerProvider.getObject().removeTool("getMemberInfo");
    return "MCP 도구를 제거했습니다: " + "getMemberInfo";
  }
}