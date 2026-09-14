package com.example.demo.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BoomBarrierTools {
  // ##### 도구 #####
  @McpTool(description = "차단기를 올립니다.")
  public String boomBarrierUp(McpSyncRequestContext context) {
    try {
      for (int second = 1; second <= 5; second++) {
        Thread.sleep(1000);
        int progressValue = second * 20;
        context.progress(progress -> progress
            .progress(progressValue)
            .total(100.0)
            .message("차단기 올리는 중")
        );
      }
    }
    catch (InterruptedException exception) {
    }
    return "차단기 올림";
  }

  @McpTool(description = "차단기를 내립니다.")
  public String boomBarrierDown() {
    return "차단기 내림";
  }
}
