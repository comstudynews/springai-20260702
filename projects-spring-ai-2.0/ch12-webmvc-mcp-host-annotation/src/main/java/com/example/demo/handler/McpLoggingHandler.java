package com.example.demo.handler;

import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpLoggingHandler {
    @McpLogging(clients = "tool-server")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        log.info("MCP Server 로깅 알림: " + notification.level() + " - " + notification.data());
    }
}
