package br.com.garage_management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParkedEventDto(

        @JsonProperty("license_plate")
        String licensePlate,
        Double lat,
        Double lng

) implements WebhookEvent {}