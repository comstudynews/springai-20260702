package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class McpSamplingService {
  private ChatClient chatClient;

  public McpSamplingService(
    ChatClient.Builder chatClientBuilder, 
    ToolCallbackProvider toolCallbackProvider
  ) {
    this.chatClient = chatClientBuilder
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .defaultTools(toolCallbackProvider)
        .build();
  }

  public String chat(String contentType, byte[] bytes) {
    // 미디어 생성
    Media media = Media.builder()
        .mimeType(MimeType.valueOf(contentType))
        .data(new ByteArrayResource(bytes))
        .build();

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        .text("""
            다음 단계별로 처리해 주세요.

            1단계: 이미지에서 '(숫자 2개~3개)-(한글 1자)-(숫자 4개)'로 구성된 차량 번호를 인식하세요. 예: 78라1234, 567바2558
            2단계: 인식된 차량 번호에서 끝에서부터 5번째 문자가 한글 완성형 음절이 아닐 경우에는 다시 1단계로 돌아가세요.
            3단계: 도구를 이용해서 차량 번호로 방문 차량 정보를 조회하고 안내 메시지를 생성하여 전광판에 표시합니다.

            최종 응답은 전광판 안내 메시지만 반환하고 추가 설명은 하지마세요.
        """)
        .media(media)
        .build();

    // LLM 호출
    String answer = chatClient.prompt()
        .messages(userMessage)
        .call()
        .content();
    return answer;
  }
}
