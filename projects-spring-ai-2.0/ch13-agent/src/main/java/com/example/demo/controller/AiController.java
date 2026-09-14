package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.agent.WeatherAgent;
import com.example.demo.multiagent.Orchestrator;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/ai")
public class AiController {
  // ##### 필드 #####
  private final WeatherAgent weatherAgent;
  private final Orchestrator orchestrator;

  // ##### 생성자 #####
  public AiController(WeatherAgent weatherAgent, Orchestrator orchestrator) {
    this.weatherAgent = weatherAgent;
    this.orchestrator = orchestrator;
  }

  // ##### 요청 매핑 메소드 #####  
  @PostMapping(
    value = "/weather-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String weatherAgent(
    HttpSession session,
    @RequestParam("question") String question) {
    String conversationId = session.getId();
    return weatherAgent.execute(conversationId, question);
  }

  @PostMapping(
    value = "/multi-agent",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String multiAgent(
    @RequestParam(name = "userId", defaultValue = "guest") String userId,
    @RequestParam("question") String question,
    HttpSession session) {
    return orchestrator.execute(userId, question, session.getId());
  }
}
