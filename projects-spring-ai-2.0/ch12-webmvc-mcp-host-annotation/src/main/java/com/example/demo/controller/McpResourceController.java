package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.McpResourceService;
import com.example.demo.service.McpResourceService.ResourceInfo;

@RestController
@RequestMapping("/mcp-resource")
public class McpResourceController {
  private McpResourceService mcpResourceService;

  public McpResourceController(McpResourceService mcpResourceService) {
    this.mcpResourceService = mcpResourceService;
  }

  @GetMapping(
      value = "/list-resources",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Map<String, List<ResourceInfo>> listResources() {
    return mcpResourceService.getListResources();
  }  

  @GetMapping(
      value = "/list-complete-args",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<String> listCompleteArgs(
    @RequestParam("uri") String uri, 
    @RequestParam("argName") String argName,
    @RequestParam("keyword") String keyword) {
    return mcpResourceService.getListResourceTemplateArgs(uri, argName, keyword);
  }

  @GetMapping(
      value = "/get-resource",
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String getResource(@RequestParam("uri") String uri) {
    return mcpResourceService.getResource(uri);
  }

  @PostMapping(
      value = "/chat",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String chat(@RequestParam("uri") String uri,
                     @RequestParam("question") String question) {
    return mcpResourceService.chat(uri, question);
  }
}