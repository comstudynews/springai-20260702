package com.example.demo.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.McpProgressService;

@RestController
@RequestMapping("/mcp-progress")
public class McpProgressController {
	private McpProgressService mcpProgressService;

    public McpProgressController(McpProgressService mcpProgressService) {
        this.mcpProgressService = mcpProgressService;
    }

    // 사용자의 질문을 처리하고 답변을 반환하는 엔드포인트
	@PostMapping(
			value = "/chat",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
			produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String chat(@RequestParam("attach") MultipartFile attach) throws IOException {
		return mcpProgressService.chat(attach.getContentType(), attach.getBytes());
	}
}
