package com.example.demo.provider;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResourceProvider {
	// 자재 관리 매뉴얼 리소스
  // @McpResource(
  //     uri = "manual://material-management", 
  //     name = "자재 관리 매뉴얼", 
  //     description = "자재의 입고, 보관, 출고 및 재고 점검 절차를 제공합니다.")	
  public String getMaterialManagementManual() {
    log.info("getMaterialManagementManual() 실행");
    return """
      [자재 관리 매뉴얼]
      1) 자재가 입고되면 담당자는 발주서와 납품서를 대조하여 품목과 수량을 확인합니다.
      2) 검수가 완료된 자재에는 식별표를 부착하고 지정된 보관 위치에 적재합니다.
      3) 유효기간이 있는 자재는 선입선출 원칙에 따라 관리합니다.
      4) 자재를 출고할 때는 승인된 출고 요청서와 실제 출고 수량을 확인해야 합니다.
      5)재고 실사는 매월 실시하며, 전산 재고와 실제 재고가 다르면 원인을 조사하고 조정 내역을 기록합니다.
      6) 파손되거나 사용이 불가능한 자재는 정상 재고와 분리하고 폐기 승인 절차를 진행합니다.
    """;
  }

  @McpResource(
      uri = "policy://{policyName}", 
      name = "회사내부 업무정책", 
      description = "회사내부 업무정책을 제공합니다.")
  public String getPolicy(String policyName) {
    if ("연차 휴가".equals(policyName)) {
      return """
        [연차 휴가]
        - 직원은 연간 15일의 휴가를 사용할 수 있습니다.
        - 휴가는 최소 3일 전에 신청해야 합니다.
        - 3일 이상의 연속 휴가는 최소 7일 전에 신청해야 합니다.
      """;
    } else if ("병가 휴가".equals(policyName)) {
      return """
        [병가 휴가]
        - 직원은 연간 최대 10일의 병가를 사용할 수 있습니다.
        - 3일 이상의 병가를 사용할 경우 진단서를 제출해야 합니다.
      """;
    } else if ("경조사 휴가".equals(policyName)) {
      return """
        [경조사 휴가]
        - 본인 결혼: 5일
        - 자녀 결혼: 1일
        - 배우자 또는 부모 사망: 5일
      """;
    } else if ("휴가 승인".equals(policyName)) {
      return """
        [휴가 승인]
        - 모든 휴가는 팀장의 승인을 받아야 합니다.
        - 긴급한 경우 사후 승인을 받을 수 있습니다.
      """;
    } else if ("근무 시간".equals(policyName)) {
      return """
        [근무 시간]
          - 기본 근무시간은 월요일부터 금요일까지입니다.
          - 근무시간은 오전 9시부터 오후 6시까지입니다.
          - 점심시간은 오후 12시부터 오후 1시까지입니다.
      """;
    } else if ("유연 근무".equals(policyName)) {
      return """
        [유연 근무]
          - 직원은 오전 8시부터 오전 10시 사이에 출근할 수 있습니다.
          - 1일 기본 근무시간은 휴게시간을 제외하고 8시간입니다.
          - 유연근무제를 이용하는 경우 팀장의 승인을 받아야 합니다.
      """;
    } else if ("초과 근무".equals(policyName)) {
      return """
        [초과 근무]
        - 초과근무는 사전에 팀장의 승인을 받아야 합니다.
        - 승인되지 않은 초과근무는 원칙적으로 인정하지 않습니다.
      """;
    } else if ("재택 근무".equals(policyName)) {
      return """
        [재택 근무]
        - 재택 근무는 주 최대 2일까지 가능합니다.
        - 재택 근무는 최소 하루 전에 신청해야 합니다.
        - 재택 근무 중에도 기본 업무시간에는 연락이 가능해야 합니다.
      """;
    } else if ("근무 기록".equals(policyName)) {	
      return """
        [근무 기록]
        - 직원은 사내 근태관리 시스템을 통해 근무 시간을 기록해야 합니다.
        - 근무 기록에 오류가 있는 경우 3일 이내에 수정 요청해야 합니다.
      """;
    } else {
      return "해당 키에 대한 리소스가 없습니다.";
    }
  }

  @McpComplete(uri = "policy://{policyName}")
  public List<String> completePolicyName(String keyword) {
    List<String> names = List.of(
      "연차 휴가", "병가 휴가", "경조사 휴가", "휴가 승인", 
      "근무 시간", "유연 근무", "초과 근무", "재택 근무", "근무 기록"
    );
    if (keyword == null || keyword.isEmpty()) {
      return names;
    } else {
      return names.stream()
          .filter(name -> name.contains(keyword))
          .toList();
    } 
  }

}
