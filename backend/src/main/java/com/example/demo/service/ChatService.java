package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        당신은 Spring AI 입문자를 돕는 한국어 튜터입니다.
                        질문의 핵심부터 설명하고, 필요한 경우 짧은 예제를 덧붙이세요.
                        확인되지 않은 내용은 단정하지 마세요.
                        """)
                .build();
    }

    public String ask(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
