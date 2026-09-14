package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  @GetMapping("/")
  public String home() {
    return "home";
  }
  
  @GetMapping("/weather-agent")
  public String weatherAgent() {
    return "weather-agent";
  }
  
  @GetMapping("/multi-agent")
  public String multiAgent() {
    return "multi-agent";
  }
}
