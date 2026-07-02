package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "질문을 입력해 주세요.")
        @Size(max = 2000, message = "질문은 2,000자 이내로 입력해 주세요.")
        String message
) {
}