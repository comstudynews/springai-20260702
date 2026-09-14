package com.example.demo.handler;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpSamplingHandler {
    private String modelName;
    private ChatClient chatClient;

    public McpSamplingHandler(ChatModel chatModel) {
        this.modelName = chatModel.getOptions().getModel();
        this.chatClient = ChatClient.builder(chatModel)
            .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
            .build();
    }

    // tool-server가 보낸 Sampling 요청을 처리
    @McpSampling(clients = "tool-server")
    public CreateMessageResult handleSampling(CreateMessageRequest request) {
        log.info("handleSampling() 실행");

        // 요청의 첫 번째 메시지에서 텍스트 프롬프트 추출
        String prompt =
            ((TextContent) request.messages()
                .get(0)
                .content())
                .text();

        // 추출한 프롬프트를 LLM에 전달하여 응답 생성
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        // CreateMessageResult 생성하고 반환
        return CreateMessageResult.builder(
                Role.ASSISTANT,
                response,
                modelName)
            .build();
    }
}
