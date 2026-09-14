package com.example.demo.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BoomBarrierTools {
  // ##### 도구 #####
  @McpTool(description = "차단기를 올립니다.")
  public Mono<String> boomBarrierUp() {
    log.info("차단기를 올립니다.");
    return Mono.just("차단기 올림");
  }

  @McpTool(description = "차단기를 내립니다.")
  public Mono<String> boomBarrierDown() {
    log.info("차단기를 내립니다.");
    return Mono.just("차단기 내림");
  }
}
