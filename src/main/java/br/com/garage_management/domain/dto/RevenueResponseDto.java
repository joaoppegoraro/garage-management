package br.com.garage_management.domain.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record RevenueResponseDto(

        BigDecimal amount,
        String currency,
        ZonedDateTime timestamp
) {}