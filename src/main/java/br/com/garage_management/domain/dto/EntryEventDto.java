package br.com.garage_management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record EntryEventDto(

        @JsonProperty("license_plate")
        String licensePlate,
        @JsonProperty("entry_time")
        LocalDateTime entryTime

) implements WebhookEvent {}