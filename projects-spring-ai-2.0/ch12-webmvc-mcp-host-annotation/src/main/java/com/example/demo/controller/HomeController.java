package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  @GetMapping("/")
  public String home() {
    return "home";
  }

  @GetMapping("/mcp-tool-changed")
  public String mcpToolChanged() {
    return "mcp-tool-changed";
  }

  @GetMapping("/mcp-logging")
  public String mcpLogging() {
    return "mcp-logging";
  }

  @GetMapping("/mcp-sampling")
  public String mcpSampling() {
    return "mcp-sampling";
  }


  @GetMapping("/mcp-elicitation")
  public String mcpElicitation() {
    return "mcp-elicitation";
  }  

  @GetMapping("/mcp-progress")
  public String mcpProgress() {
    return "mcp-progress";
  }

  @GetMapping("/mcp-resource")
  public String mcpResource() {
    return "mcp-resource";
  }

  @GetMapping("/mcp-prompt")
  public String mcpPrompt() {
    return "mcp-prompt";
  }
}
