package br.com.garage_management.domain.dto;

import java.util.List;

public record GarageConfigDto(

        List<GarageSectorDto> garage,
        List<ParkingSpotDto> spots
) {}