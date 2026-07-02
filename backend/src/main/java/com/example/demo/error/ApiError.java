package com.example.demo.error;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String message
) {
}