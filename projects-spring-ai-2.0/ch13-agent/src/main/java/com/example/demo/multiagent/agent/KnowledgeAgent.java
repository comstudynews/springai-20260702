package com.example.demo.multiagent.agent;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.demo.multiagent.State;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KnowledgeAgent {
  // 에이전트의 역할과 행동 범위를 정의하는 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
  		당신은 지식 베이스 관리 전문가입니다.
  		다음 단계별로 처리해야 합니다.
  		1) 주어진 키워드를 분석해서 다음 카테고리 중 하나로 분류하세요.
	      - 기술지원, 주문배송, 환불교환, 계정관리, 일반문의
	    2) 분류 문자열을 매개값으로 도구를 호출해서 내용을 검색하세요.
	    3) 검색 결과를 바탕으로 사용자에게 답변을 작성하세요.
  		""";

  // ChatClient를 사용하여 LLM과 상호작용
  private final ChatClient chatClient;

  // ChatClient 초기화하는 생성자
  public KnowledgeAgent(ChatClient.Builder builder) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .build();
  }

  // 에이전트를 실행하는 메서드
  public String execute(String keyword) {
  	log.info("KnowledgeAgent 실행");
    String response = chatClient.prompt()
        .user(keyword)
        .tools(this)
        .call()
        .content();
    return response;
  }
  
  public State execute(State state) {
    String response = execute(state.getAnalysisResult().getKeywords());
    state.setKnowledgeBase(response);
    return state;
  }

  // 도구 정의
  @Tool(description = "지식 베이스에서 관련 문서를 검색합니다.")
  public String tool(
  		@ToolParam(description = "기술지원, 주문배송, 환불교환, 계정관리, 일반문의 중의 하나") 
  		String category) {
  	log.info("KnowledgeAgent.tool 실행: {}", category);
		String result = knowledgeBase.get(category);
		if (result != null) {
			return result;
		} else {
			return "";
		}
  }
  
  // Mock 지식 베이스 데이터
  private final Map<String, String> knowledgeBase = Map.of(
      "기술지원", """
      📚 기술 지원 FAQ:
		      - 제품 초기화: 설정 > 시스템 > 공장 초기화
		      - 로그 전송: 앱 오류 시 로그 자동 전송
		      - 업데이트: 앱 > 설정 > 버전 확인
		      """,  		
      "주문배송", """
          📚 배송 정책:
          - 평일 오후 2시 이전 주문 시 당일 출고
          - 배송 기간: 1-3일 (도서산간 2-4일)
          - 배송 추적은 마이페이지에서 확인 가능
          - 배송비: 3만원 이상 무료, 미만 3,000원
          """,
      "환불교환", """
          📚 환불 정책:
          - 구매 후 7일 이내 환불 가능
          - 미개봉 제품에 한함
          - 환불 금액은 영업일 기준 3-5일 소요
          - 배송비는 고객 부담 (제품 하자 시 무료)
          """,
      "계정관리", """
          📚 계정 관리 가이드:
          - 비밀번호 재설정: 로그인 > 비밀번호 찾기
          - 회원 탈퇴: 마이페이지 > 설정 > 회원 탈퇴
          - 개인정보 수정: 마이페이지 > 정보 수정
          - 이메일/문자 알림 설정 가능
          """
  );
}

