package com.example.demo.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.McpSamplingService;

@RestController
@RequestMapping("/mcp-sampling")
public class McpSamplingController {
  private McpSamplingService mcpSamplingService;

  public McpSamplingController(McpSamplingService mcpSamplingService) {
    this.mcpSamplingService = mcpSamplingService;
  }

  @PostMapping(
      value = "/chat",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String chat(
      @RequestParam("attach") MultipartFile attach
  ) throws IOException {
    return mcpSamplingService.chat(attach.getContentType(), attach.getBytes());
  }
}