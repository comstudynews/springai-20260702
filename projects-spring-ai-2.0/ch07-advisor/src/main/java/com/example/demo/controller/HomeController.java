package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  @GetMapping("/")
  public String home() {
    return "home";
  }

  @GetMapping("/advisor-chain-call")
  public String advisorChainCall() {
    return "advisor-chain-call";
  }
  
  @GetMapping("/advisor-chain-stream")
  public String advisorChainStream() {
    return "advisor-chain-stream";
  }  
  
  @GetMapping("/advisor-context")
  public String advisorContext() {
    return "advisor-context";
  }  
  
  @GetMapping("/advisor-logging")
  public String advisorLogging() {
    return "advisor-logging";
  }  
  
  @GetMapping("/advisor-safe-guard")
  public String advisorSafeGuard() {
    return "advisor-safe-guard";
  }  
}
