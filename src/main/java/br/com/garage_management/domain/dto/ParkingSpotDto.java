package br.com.garage_management.domain.dto;

public record ParkingSpotDto(

        Long id,
        String sector,
        Double lat,
        Double lng
) {}