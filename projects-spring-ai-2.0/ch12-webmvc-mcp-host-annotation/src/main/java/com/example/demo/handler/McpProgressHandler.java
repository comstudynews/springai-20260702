package com.example.demo.handler;

import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpProgressHandler {
    @McpProgress(clients = "tool-server")
    public void handleProgressNotification(ProgressNotification notification) {
        log.info(
            notification.progressToken() + ": " + 
            notification.message() + "(" + notification.progress() + "%)");
    }
}