package com.example.demo.handler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class McpElicitationHandler {

  // Elicitation 요청 내용과 처리 상태를 관리하는 레코드
  public static record PendingElicitation(
    // 작업 ID
    String taskId,
    // Elicitation 요청 메시지
    String message,
    // 사용자의 승인 또는 거부 결과를 기다리는 비동기 결과 객체
    CompletableFuture<ElicitResult> future 
  ) {}

  // 모든 Elicitation 요청을 관리하는 맵
  private Map<Long, PendingElicitation> pendingRequests = new ConcurrentSkipListMap<>();

  // Elicitation 요청 식별 ID를 생성
  private final AtomicLong idGenerator = new AtomicLong();


  @McpElicitation(clients = "tool-server")
  public ElicitResult handleElicitation(ElicitRequest elicitRequest) {
    // MCP 서버가 메타데이터로 돌려준 작업 ID를 확인
    Object taskIdValue = elicitRequest.meta().get("taskId");
    if (!(taskIdValue instanceof String taskId) || taskId.isBlank()) {
      // 작업 ID가 없으면 Elicitation 요청을 취소로 처리
      return new ElicitResult(Action.CANCEL, null);
    }

    // Elicitation 요청 식별 ID 생성
    long id = idGenerator.incrementAndGet();

    // 사용자의 승인 또는 거부 결과를 기다리는 비동기 결과 객체 생성
    CompletableFuture<ElicitResult> future = new CompletableFuture<>();

    // Elicitation 요청 식별 ID를 키로해서 PendingElicitation을 맵에 저장
    pendingRequests.put(id, new PendingElicitation(taskId, elicitRequest.message(), future));

    try {
      // 비동기 결과가 완료할 때까지 현재 스레드에서 최대 60초간 기다림
      return future.get(60, TimeUnit.SECONDS);
    } catch (Exception exception) {
      // 시간 초과, 대기 중 인터럽트, 비동기 처리 실패는 모두 사용자 거부로 간주
      // 거부가 되면 LLM은 모델의 판단에 따라 승인 요청을 다시 할 수 있음
      return new ElicitResult(Action.CANCEL, null);
    } finally {
      // 처리가 끝난 PendingElicitation 객체를 맵에서 제거
      pendingRequests.remove(id);
    }
  }

  // 브라우저 폴링 응답으로 전달할 레코드
  public record PendingRequest(long id, String message) {}

  // 작업 ID와 일치하는 PendingRequest 반환
  // - 응답: {"id": "Elicitation 요청 식별 ID", "message": "Elicitation 메시지"}
  public PendingRequest findPendingRequest(String taskId) {
    return pendingRequests.entrySet().stream()
      // taskId가 일치하는 PendingElicitation들을 찾음
      // 하나의 작업 ID에 여러 개의 Elicitation 요청이 있을 수 있음
      .filter(entry -> entry.getValue().taskId().equals(taskId))
      // 가장 먼저 저장된 PendingElicitation을 반환
      .findFirst()
      // PendingRequest 레코드로 변환하여 반환, 없으면 null 반환
      .map(entry -> new PendingRequest(entry.getKey(), entry.getValue().message()))
      .orElse(null);
  }

  // Elicitation 요청에 대한 응답 처리
  public boolean respondToMcpServer(long id, String taskId, boolean approved) {
    // 해당 ID의 Elicitation 요청이 없거나 작업 ID가 일치하지 않으면 바로 false 반환
    PendingElicitation pendingElicitation = pendingRequests.get(id);
    if (pendingElicitation == null || !pendingElicitation.taskId().equals(taskId)) {
      return false;
    }

    // Elicitation 요청에 대한 EliciResult 응답을 생성
    // - 사용자 승인 시 ACCEPT와 구조화 응답 값을 전달하고, 거부 시 DECLINE만 전달
    ElicitResult elicitResult = approved
      ? new ElicitResult(Action.ACCEPT, Map.of("approved", true))
      : new ElicitResult(Action.DECLINE, null);

    // ElicitResult를 전달하여 비동기 결과 객체를 완료하고, 완료 성공 여부를 반환
    return pendingElicitation.future().complete(elicitResult);
  }
}