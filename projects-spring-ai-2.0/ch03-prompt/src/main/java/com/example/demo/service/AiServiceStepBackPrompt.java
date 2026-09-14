package com.example.demo.service;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceStepBackPrompt {
  // ##### 필드 #####
  private ChatClient chatClient;

  // ##### 생성자 #####
  public AiServiceStepBackPrompt(ChatClient.Builder chatClientBuilder) {
    chatClient = chatClientBuilder
    		// 추론 속도를 높이기 위해 추론 노력 수준을 "low"로 설정
    		.defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
    		.build();
  }

  // ##### 메소드 #####
  public String stepBackPrompt(String question) throws Exception {
    String questions = chatClient.prompt()
    		.system("""
            사용자 질문을 처리할 때 Step-Back 프롬프트 기법을 사용하려고 합니다.
            
            규칙:
            1)사용자 질문을 단계별 질문들로 재구성해주세요. 
            2)추가 작업을 제안하거나 사용자의 의사를 묻는 질문은 포함시키지 마세요.
            3)맨 마지막 질문은 사용자 질문과 일치해야 합니다.
            
            출력형식: 단계별 질문을 항목으로 하는 JSON 배열로 출력
            예시: ["...", "...", "...", "..."]    
            """)
        .user("사용자 질문: %s".formatted(question))
        .call()
        .content();
  
    String json = questions.substring(questions.indexOf("["), questions.indexOf("]")+1);
    log.info(json);
    
    ObjectMapper objectMapper = new ObjectMapper();
    List<String> listQuestion = objectMapper.readValue(
        json,
        new TypeReference<List<String>>() {}
    );
    
    String[] answerArray = new String[listQuestion.size()];
    for(int i=0; i<listQuestion.size(); i++) {
      String stepQuestion = listQuestion.get(i);
      String stepAnswer = getStepAnswer(stepQuestion, answerArray);
      answerArray[i] = stepAnswer;
      log.info("----------------------------------------------------");
      log.info("단계{} 질문: {}, 답변: {}", i+1, stepQuestion, stepAnswer);
    }
    
    return answerArray[answerArray.length-1];
  }

  public String getStepAnswer(String question, String... prevStepAnswers) {
    String context = "";
    for (String prevStepAnswer : prevStepAnswers) {
      context += Objects.requireNonNullElse(prevStepAnswer, "");
    }
    String answer = chatClient.prompt()
    		.system("""
    				다음 규칙을 지켜서 답변하세요.
    				규칙:
    				1) 사용자의 질문이 정보가 다소 부족하더라도 사용자에게 묻는 추가 질문을 하지 마세요. 
            2) 합리적인 가정을 세운 뒤 최선의 답변을 생성하세요.
            3) 답변 마지막에 추가 작업을 제안하거나 사용자의 의사를 묻지 마세요.
            4) "원하시면", "필요하시면", "원한다면" 등의 후속 제안을 하지 마세요.
    				""")
        .user("""
            %s
            문맥: %s
            """.formatted(question, context))
        .call()
        .content();
    return answer;
  }
}
