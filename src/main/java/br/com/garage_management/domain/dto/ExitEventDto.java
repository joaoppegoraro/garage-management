package br.com.garage_management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ExitEventDto(

        @JsonProperty("license_plate")
        String licensePlate,
        @JsonProperty("exit_time")
        LocalDateTime exitTime

) implements WebhookEvent {}