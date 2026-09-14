package com.example.demo.multiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.example.demo.multiagent.State;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AnalysisAgent {
  // 에이전트의 역할과 행동 범위를 정의하는 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
	  당신은 고객 문의 분석 전문가입니다. 
	  고객의 문의 내용을 분석하여 다음과 같은 정보를 추출해야 합니다:
	  1. keywords: 문의의 핵심 키워드들을 쉼표로 구분하여 나열하세요.
	  2. intent: 고객이 문의를 통해 원하는 것이 무엇인지 간단히 설명하세요.
	  3. emotion: 고객의 감정 상태를 불만, 중립, 긍정 등으로 분류하세요.
	  4. urgency: 문의의 긴급도를 높음, 보통, 낮음으로 평가하세요.
	  5. inquiryType: 다음 카테고리 중 하나로 문의 유형을 분류하세요.
	      - 기술지원: 제품 작동 오류, 고장, 버그, 설정 문제, 기능 문의
	      - 주문배송: 주문 조회, 배송 추적, 배송 지연, 출고 문의
	      - 환불교환: 환불 요청, 교환 요청, 취소, 반품
	      - 계정관리: 로그인, 비밀번호, 회원 정보 수정, 탈퇴
	      - 일반문의: 위 카테고리에 해당하지 않는 모든 문의
  """;

  // ChatClient를 사용하여 LLM과 상호작용
  private final ChatClient chatClient;

  // 생성자
  public AnalysisAgent(ChatClient.Builder builder) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .build();
  }

  // 에이전트를 실행하는 메서드
  public AnalysisResult execute(State state) {
  	log.info("AnalysisAgent 실행");
  	AnalysisResult analysisResult = chatClient.prompt()
        .user(state.getUserInquiry())
        .call()
        .entity(AnalysisResult.class);
  	if(state != null) {
			state.setAnalysisResult(analysisResult);
		}
  	return analysisResult;
  }

  // 분석 결과를 담는 내부 클래스
  @Data
  public static class AnalysisResult {
    private String keywords;
    private String intent;
    private String emotion;
    private String urgency;
    private String inquiryType;
  }
}

