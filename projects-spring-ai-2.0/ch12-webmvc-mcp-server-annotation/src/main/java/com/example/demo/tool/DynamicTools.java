package com.example.demo.tool;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DynamicTools {
	private Map<String, Member> members = Map.of(
		"member1", new Member("member1", "홍길동", "hong@example.com"),
		"member2", new Member("member2", "스프링", "spring@example.com")
	);

	public String getMemberInfo(String mid) {
		Member member = members.get(mid);
		return member == null
				? "회원 정보를 찾을 수 없습니다: " + mid
				: "회원 ID: %s, 이름: %s, 이메일: %s"
						.formatted(member.mid(), member.name(), member.email());
	}

	private record Member(String mid, String name, String email) {}
}
