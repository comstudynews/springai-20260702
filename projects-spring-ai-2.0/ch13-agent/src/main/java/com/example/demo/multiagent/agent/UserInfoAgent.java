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
public class UserInfoAgent {
  // 에이전트의 역할과 행동 범위를 정의하는 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
  		당신은 사용자 정보 관리 전문가입니다.
  		도구를 사용하여 사용자 정보를 조회하세요.
  		조회된 사용자 정보만 출력하고 설명, 질문, 제안 또는 후속 조치 안내는 추가하지 마세요.
  		
  		답변 반드시 다음과 같은 형식으로 작성하세요.
  		
	    ###### 안녕하세요. 
	    <사용자 이름> 고객님의 정보는 다음과 같습니다.	
  		
  		- 아이디: <사용자 아이디>  		
  		- 이름: <사용자 이름>  		
  		- 등급: <사용자 등급>  		
  		- 누적주문: <사용자 누적주문>
  		- 보유포인트: <사용자 보유포인트>
  		
  		감사합니다.
  		""";

  // ChatClient를 사용하여 LLM과 상호작용
  private final ChatClient chatClient;

  // ChatClient 초기화하는 생성자
  public UserInfoAgent(ChatClient.Builder builder) {
    this.chatClient = builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultOptions(OpenAiChatOptions.builder().reasoningEffort("low"))
        .build();
  }

  // 에이전트를 실행하는 메서드
  public String execute(String userId) {
    log.info("UserInfoAgent 실행");
    String response = chatClient.prompt()
        .user("""
            다음 사용자 ID에 대한 정보를 조회하세요
            사용자 ID: %s
            """.formatted(userId))
        .tools(this)
        .call()
        .content();
    return response;
  }
  
  public State execute(State state) {
    String response = execute(state.getUserId());
    state.setUserInfo(response);
    return state;
  }

  // 도구 정의
  @Tool(description = "사용자 정보를 조회합니다.")
  public String getUserInfo(@ToolParam(description = "사용자 ID") String userId) {
    log.info("UserInfoAgent.tool 실행: {}", userId);
    String user = userDb.getOrDefault(userId, userDb.get("guest"));
    return user;
  }  

  // Mock 사용자 데이터
  private final Map<String, String> userDb = Map.of(
    "guest", """
        {"아이디": "guest", "이름": "게스트", "등급": "일반", "누적주문": 0, "보유포인트": 0}
        """,
    "user1", """
        {"아이디": "user1", "이름": "김철수", "등급": "골드", "누적주문": 15, "보유포인트": 12500}
        """,
    "user2", """
        {"아이디": "user2", "이름": "채정원", "등급": "VIP", "누적주문": 50, "보유포인트": 85000}
        """
  );
}

