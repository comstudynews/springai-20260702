package com.example.demo.multiagent;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.stereotype.Component;

import com.example.demo.multiagent.agent.AccountAgent;
import com.example.demo.multiagent.agent.AnalysisAgent;
import com.example.demo.multiagent.agent.GeneralAgent;
import com.example.demo.multiagent.agent.KnowledgeAgent;
import com.example.demo.multiagent.agent.OrderAgent;
import com.example.demo.multiagent.agent.RefundAgent;
import com.example.demo.multiagent.agent.TechSupportAgent;
import com.example.demo.multiagent.agent.UserInfoAgent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Orchestrator {
	// 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
    당신은 고객지원 멀티에이전트 시스템의 오케스트레이터입니다.
    반드시 도구를 호출해서 답변을 얻어서 사용자에게 전달해야 합니다.
    도구 호출없이 임의로 답변을 생성하지 마세요.
    
    [도구 선택 규칙]
    1. 사용자가 이름, 등급, 가입일 등 사용자 정보 자체만 요청한 경우에만
       callUserInfoAgent를 호출하세요.
    2. 기술지원, 주문/배송, 환불/교환, 계정관리, 지식 검색 또는 여러 문제가
       포함된 문의에는 반드시 callMultiAgent를 호출하세요.
  	""";
  
	// ChatClient를 사용하여 LLM과 상호작용
  private final ChatClient chatClient;
	
	// 전문 에이전트
  private final AnalysisAgent analysisAgent;
  private final UserInfoAgent userInfoAgent;
  private final KnowledgeAgent knowledgeAgent;
  private final TechSupportAgent techSupportAgent;
  private final OrderAgent orderAgent;
  private final RefundAgent refundAgent;
  private final AccountAgent accountAgent;
  private final GeneralAgent generalAgent;

  // 생성자
	public Orchestrator(
	    ChatClient.Builder chatClientBuilder,
	    ChatMemory chatMemory,
	    AnalysisAgent analysisAgent,
	    UserInfoAgent userInfoAgent,
	    KnowledgeAgent knowledgeAgent,
	    TechSupportAgent techSupportAgent,
	    OrderAgent orderAgent,
	    RefundAgent refundAgent,
	    AccountAgent accountAgent,
	    GeneralAgent generalAgent) {
	  this.chatClient = chatClientBuilder
	      .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
	      .build();
	  this.analysisAgent = analysisAgent;
	  this.userInfoAgent = userInfoAgent;
	  this.knowledgeAgent = knowledgeAgent;
	  this.techSupportAgent = techSupportAgent;
	  this.orderAgent = orderAgent;
	  this.refundAgent = refundAgent;
	  this.accountAgent = accountAgent;
	  this.generalAgent = generalAgent;
	}

  // 도구 정의  
	@Tool(description = "사용자 정보를 조회합니다.", 
				returnDirect = true,
				/*
				- returnDirect = true인 경우 반환값을 변환하기 위해 ToolCallResultConverter가 기본적으로 사용
				- 기본 변환기는 반환값을 JSON 형태로 직렬화할 수 있어서 따옴표나 JSON 이스케이프가 포함될 수 있음
				- 따라서 반환값이 String이고 이를 JSON 직렬화 없이 순수 텍스트 그대로 반환하려면
				- PlainTextToolCallResultConverter와 같은 사용자 정의 ToolCallResultConverter를 사용해야함
				*/
	      resultConverter = PlainTextToolCallResultConverter.class)
	public String callUserInfoAgent(
			@ToolParam(description = "사용자 ID") String userId) {
		log.info("callUserInfoAgent: userId={}", userId);	
		String userInfo = userInfoAgent.execute(userId);
		return userInfo;
	}
  
  @Tool(description = "복합적인 사용자 문의를 분석하고 분석 결과에 따라 답변을 생성할 때 호출할 수 있습니다.",
        returnDirect = true,
        resultConverter = PlainTextToolCallResultConverter.class)
  public String callMultiAgent(
      @ToolParam(description = "사용자 ID") String userId,
      @ToolParam(description = "사용자 문의") String userInquiry) {
    log.info("callMultiAgent: userId={}, userInquiry={}", userId, userInquiry);

    State state = new State();
    state.setUserId(userId);
    state.setUserInquiry(userInquiry);

    // 지식 검색에 분석 결과가 필요하므로 문의 분석을 먼저 실행한다.
    analysisAgent.execute(state);

    // 두 에이전트는 서로 다른 State 필드를 갱신하므로 병렬 실행할 수 있다.
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      CompletableFuture<State> userInfoFuture = CompletableFuture.supplyAsync(
          () -> userInfoAgent.execute(state), executor);
      CompletableFuture<State> knowledgeFuture = CompletableFuture.supplyAsync(
          () -> knowledgeAgent.execute(state), executor);

      CompletableFuture.allOf(userInfoFuture, knowledgeFuture).join();
    }

    // 분석 결과에 따라 답변 생성을 위한 적절한 전문 에이전트를 호출한다.
    String inquiryType = state.getAnalysisResult().getInquiryType();
    switch (inquiryType == null ? "일반문의" : inquiryType.trim()) {
      case "기술지원" -> techSupportAgent.execute(state);
      case "주문배송" -> orderAgent.execute(state);
      case "환불교환" -> refundAgent.execute(state);
      case "계정관리" -> accountAgent.execute(state);
      default -> generalAgent.execute(state);
    }
    
    log.info("state: {}", state);
    
    return state.getFinalResponse();
  }
  
	public static class PlainTextToolCallResultConverter implements ToolCallResultConverter {
	  @Override
	  public String convert(Object result, Type returnType) {
	    return Objects.toString(result, "");
	  }
	}
  
  // Orchestrator 실행
public String execute(String userId, String userInquiry, String conversationId) {
  String response = chatClient.prompt()
  		.system(SYSTEM_PROMPT)
      .user("""
      		다음 사용자 ID의 문의 내용에 대해 답변해주세요.
      		사용자 ID: %s
          사용자 문의: %s
      		""".formatted(userId, userInquiry))
      .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
      .tools(this)
      .call()
      .content();
  return response;
}  
}


















