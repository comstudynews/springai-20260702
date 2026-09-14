package com.example.demo.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SignboardTools {
    @McpTool(
        description = "방문 차량 정보를 조회하고 안내 메시지를 생성하여 전광판에 표시합니다."
    )
    public String displayMessage(
            McpSyncRequestContext context,
            @McpToolParam(description = "방문 차량 번호") String vehicleNumber) {

        // 방문 차량 정보 조회
        String vehicleInfo = findVehicleInfo(vehicleNumber);

        // MCP Client가 Sampling 기능을 지원하는지 확인
        if (!context.sampleEnabled()) {
            return "MCP Client가 Sampling 기능을 지원하지 않습니다.";
        }

        // MCP Client에 Sampling 요청
        CreateMessageResult samplingResult = context.sample(sample -> sample
            .message("""
                다음 방문 차량 정보를 바탕으로
                차량 출입구 전광판에 표시할 안내 메시지를 하나만 작성해주세요.
                추가적으로 설명과 질문은 하지 마시고, 안내 메시지만 작성해주세요.

                [제한 사항]
                - 안내 메시지는 30자 이내로 작성해야 합니다.

                [방문 차량 정보]
                %s

                안내 메시지는 방문자가 쉽게 이해할 수 있도록
                짧고 친절한 문장으로 작성해주세요.
                """.formatted(vehicleInfo)));

        // Sampling 결과에서 LLM이 생성한 텍스트 추출
        String message = ((TextContent) samplingResult.content()).text();

        // 전광판에 안내 메시지 표시
        showOnSignboard(message);

        // LLM으로 생성된 안내 메시지 반환
        return message;
    }

    private String findVehicleInfo(String vehicleNumber) {
        // 실제 프로젝트에서는 DB 또는 외부 시스템에서 조회
        return switch (vehicleNumber) {
            case "23가4567" -> """
                차량번호: 23가4567
                방문자: 홍길동
                방문목적: 장비 점검
                방문부서: 연구개발팀
                주차위치: B동 방문자 주차장
                """;
            case "234가5678" -> """
                차량번호: 234가5678
                방문자: 김영희
                방문목적: 납품
                방문부서: 자재관리팀
                주차위치: 물류센터 하역장
                """;
            case "345가6789" -> """
                차량번호: 345가6789
                방문자: 이민수
                방문목적: 업무 협의
                방문부서: 경영지원팀
                주차위치: A동 방문자 주차장
                """;
            default -> """
                차량번호: %s
                조회결과: 등록된 방문 차량이 아닙니다.
                """.formatted(vehicleNumber);
        };
    }

    private void showOnSignboard(String message) {
        // 실제 프로젝트에서는 전광판 제어 API 호출
        log.info("================================");
        log.info("[전광판]");
        log.info(message);
        log.info("================================");
    }
}
