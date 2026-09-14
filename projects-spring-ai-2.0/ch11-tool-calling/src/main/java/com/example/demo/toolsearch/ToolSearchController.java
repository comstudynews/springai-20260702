package com.example.demo.toolsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/ai")
@Slf4j
public class ToolSearchController {
  // ##### 필드 #####
  @Autowired
  private ToolSearchService toolSearchService;
  
  // ##### 요청 매핑 메소드 #####
  @PostMapping(
    value = "/tool-search",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String dateTimeTools(@RequestParam("question") String question, HttpSession session) {
    String answer = toolSearchService.chat(question, session.getId());
    return answer;
  }  
}

