package br.com.garage_management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public record GarageSectorDto(
        String sector,

        @JsonProperty("base_price")
        Double basePrice,

        @JsonProperty("max_capacity")
        int maxCapacity,

        @JsonProperty("open_hour")
        LocalTime openHour,

        @JsonProperty("close_hour")
        LocalTime closeHour,

        @JsonProperty("duration_limit_minutes")
        Integer durationLimitMinutes
) {}