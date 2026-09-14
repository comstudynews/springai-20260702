package com.example.demo.tool;

import java.util.List;
import java.util.Vector;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CarCheckTools {
  // ##### 필드 #####
  //데이터베이스에 다음과 같이 차량 번호가 등록되어 있다고 가정
  private List<String> carNumbers = new Vector<>(List.of(
    "23가4567", "234부8372", "345가6789"
  ));

  // ##### 도구 #####
  @McpTool(description = "인식된 차량 번호가 등록되어 있는지 확인합니다.")
  public boolean checkCarNumber(@McpToolParam(description = "차량 번호") String carNumber) {
    //차량 번호에 포함된 모든 공백 제거
    carNumber = carNumber.replaceAll("\\s+", "");
    log.info("LLM이 인식한 차량 번호: {}", carNumber);
    //데이터베이스에 차량 번호가 등록되어 있는지 확인
    boolean result = carNumbers.contains(carNumber);
    return result;
  }

  @McpTool(description = "차량 번호를 등록합니다.")
  public boolean registerCarNumber(@McpToolParam(description = "등록할 차량 번호") String carNumber) {
    //차량 번호에 포함된 모든 공백 제거
    carNumber = carNumber.replaceAll("\\s+", "");
    log.info("등록할 차량 번호: {}", carNumber);
    //등록되지 않은 차량 번호인 경우에만 추가
    if (carNumber.isBlank() || carNumbers.contains(carNumber)) {
      return false;
    }
    return carNumbers.add(carNumber);
  }
}
