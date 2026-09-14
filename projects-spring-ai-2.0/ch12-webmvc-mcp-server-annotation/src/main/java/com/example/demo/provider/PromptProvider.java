package com.example.demo.provider;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// 프롬프트와 자동완성을 제공하는 컴포넌트
@Component
@Slf4j
public class PromptProvider {
	// 매개값 없는 MCP 프롬프트 정의 
  @McpPrompt(
      name = "spring-ai-explanation-prompt",
      description = "Spring AI의 개념과 사용법을 이해하기 쉽게 설명하도록 안내하는 프롬프트")
  public String getSpringAiExplanationPrompt() {
    return """
      'Spring AI' 전문가로서 사용자의 질문에 답변해 주세요.
      핵심 개념만 300자 이내로 설명하세요.
      """;   
  }

	// device 인수를 받아 차량 출입 제어용 프롬프트 텍스트를 생성하여 반환
  @McpPrompt(
      name = "vehicle-access-control-prompt", 
      description = """
        차량 이미지에서 번호판을 인식하고 번호 형식의 유효성을 검증한 뒤,
        인식된 차량 번호가 등록 차량인지 도구를 사용해 조회하여 결과에 따라
        출입 제어 장치를 올리거나 내리도록 안내하는 프롬프트
      """)
  public String getVehicleAccessControlPrompt(
      // @McpArg: MCP 클라이언트가 프롬프트 호출 시 전달해야 하는 필수 인수 선언
      @McpArg(name = "device", required = true) String arg) {
    return """
      다음 단계별로 처리해 주세요.

      1단계: 이미지에서 '(숫자 2개~3개)-(한글 1자)-(숫자 4개)'로 구성된 차량 번호를 인식하세요. 예: 78라1234, 567바2558
      2단계: 인식된 차량 번호에서 끝에서부터 5번째 문자가 한글 완성형 음절이 아닐 경우에는 다시 1단계로 돌아가세요.
      3단계: 1단계에서 인식된 차량 번호가 등록된 차량 번호인지 도구로 확인을 하세요.
      4단계: 3단계의 결과가 false 라면 도구로 %s를 내리고, true 라면 도구로 %s를 올리세요.

      최종 답변은 %s 내림 또는 %s 올림으로 하고 추가 설명은 하지마세요.
      """.formatted(arg, arg, arg, arg);
  }

	// device 매개값에 대한 후보 목록 반환
  @McpComplete(prompt = "vehicle-access-control-prompt")
  public List<String> completeDevice(String keyword) {
    List<String> devices = List.of("차단기", "차단봉", "셔터", "볼라드", "게이트");
    // keyword가 없으면 전체 목록, 있으면 포함 여부로 필터링
    if (keyword == null || keyword.isEmpty()) {
      return devices;
    }
    return devices.stream()
        .filter(d -> d.contains(keyword))
        .toList();
  }

	// 동적 프롬프트를 위한 
  public String getMultilingualAnswerPrompt(String language) {
    return """
      사용자의 질문에 %s로 답변해 주세요.
      """.formatted(language);
  }

@McpComplete(prompt = "multilingual-answer-prompt")
public List<String> completeLanguage(String keyword) {
  List<String> languages = List.of(
      "한국어", "영어", "일본어", "독일어", "중국어", "프랑스어");
  if (keyword == null || keyword.isEmpty()) {
    return languages;
  }
  return languages.stream()
      .filter(language -> language.contains(keyword))
      .toList();
}
}
