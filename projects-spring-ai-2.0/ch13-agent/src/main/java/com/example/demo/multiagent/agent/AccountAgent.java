package com.example.demo.multiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import com.example.demo.multiagent.State;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountAgent {
  // 에이전트의 역할과 행동 범위를 정의하는 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
      당신은 계정 관리 전문가입니다.
      로그인, 비밀번호, 개인정보 수정, 회원 탈퇴 문의에 친절하고 전문적으로 답변하세요.
  		추가 정보가 필요하더라도 고객에게 질문하거나 정보 제공 및 선택을 요청하지 마세요.
      
	    답변 반드시 다음과 같은 형식으로 작성하세요.
	
	    ###### 안녕하세요, 계정 관리팀입니다.
      고객의 이름을 사용하여 친근하게 인사합니다.
	
  		###### 해결 방법
      고객 문의와 관련된 내용을 지식 베이스 정보에서 찾아 번호 순서로 안내합니다.
      관련된 지식 베이스 정보가 없으면 확인된 해결 방법이 없다고 명확히 안내합니다.
	
      ###### 추가 문의
      추가적인 문의사항이 있을 경우 아래 연락처로 문의하시기 바랍니다.
      - 전화: 1588-1234 (평일 9-18시)
      - 이메일: support@company.com
      - 카카오톡: kakaotalk
      
	    감사합니다.
  		""";

  // ChatClient를 사용하여 LLM과 상호작용
  private final ChatClient chatClient;

  // ChatClient 초기화하는 생성자
  public AccountAgent(ChatClient.Builder builder) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .build();
  }

  // 에이전트를 실행하는 메서드
  public State execute(State state) {
  	log.info("AccountAgent 실행");
    String response = chatClient.prompt()
        .user("""
            다음 정보를 참고하여 고객에게 전달할 계정 관리팀 답변을 작성하세요.
            고객 문의: %s
            문의 분석: %s
            사용자 정보: %s
            지식 베이스 정보: %s
            """.formatted(state.getUserInquiry(), 
													state.getAnalysisResult(), 
            							state.getUserInfo(),
            							state.getKnowledgeBase()))
        .call()
        .content();
    state.setFinalResponse(response);
    return state;
  } 
}

