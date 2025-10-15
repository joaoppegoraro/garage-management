package br.com.garage_management.util;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.GarageConfigDto;
import br.com.garage_management.domain.dto.GarageSectorDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.domain.dto.ParkingSpotDto;
import br.com.garage_management.domain.dto.RevenueResponseDto;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.model.ParkingSpace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public final class TestFactoryUtil {

    private TestFactoryUtil() {
    }

    public static EntryEventDto createEntryEventDto(String licensePlate, LocalDateTime entryTime) {
        return new EntryEventDto(licensePlate, entryTime);
    }

    public static ParkedEventDto createParkedEventDto(String licensePlate) {
        return new ParkedEventDto(licensePlate, 10.0, 20.0);
    }

    public static ExitEventDto createExitEventDto(String licensePlate, LocalDateTime exitTime) {
        return new ExitEventDto(licensePlate, exitTime);
    }

    public static GarageSector createMockGarageSector(String sectorName, int occupiedCount, int maxCapacity, double basePrice) {
        GarageSector sector = new GarageSector();
        sector.setSector(sectorName);
        sector.setOccupiedCount(occupiedCount);
        sector.setMaxCapacity(maxCapacity);
        sector.setBasePrice(basePrice);
        return sector;
    }

    public static ParkingSpace createMockParkingSpace(long id, boolean isOccupied, GarageSector sector) {
        ParkingSpace space = new ParkingSpace();
        space.setId(id);
        space.setIsOccupied(isOccupied);
        space.setGarageSector(sector);
        return space;
    }

    public static ParkingRecord createMockParkingRecord(LocalDateTime entryTime, double priceOnEntry, ParkingSpace space, GarageSector sector) {
        ParkingRecord record = new ParkingRecord();
        record.setEntryTime(entryTime);
        record.setPriceAppliedOnEntry(priceOnEntry);
        record.setParkingSpace(space);
        record.setGarageSector(sector);
        return record;
    }

    public static GarageConfigDto createValidGarageConfigDto() {
        var sectorDtoA = new GarageSectorDto("A", 10.0, 100, LocalTime.MIN, LocalTime.MAX, null);
        var sectorDtoB = new GarageSectorDto("B", 12.0, 50, LocalTime.MIN, LocalTime.MAX, null);
        var spotDto1 = new ParkingSpotDto(1L, "A", 1.0, 1.0);
        var spotDto2 = new ParkingSpotDto(2L, "B", 2.0, 2.0);

        return new GarageConfigDto(List.of(sectorDtoA, sectorDtoB), List.of(spotDto1, spotDto2));
    }

    public static GarageConfigDto createMismatchedGarageConfigDto() {
        var sectorDtoA = new GarageSectorDto("A", 10.0, 100, LocalTime.MIN, LocalTime.MAX, null);
        var spotDtoValid = new ParkingSpotDto(1L, "A", 1.0, 1.0);
        var spotDtoMismatched = new ParkingSpotDto(2L, "C", 3.0, 3.0);

        return new GarageConfigDto(List.of(sectorDtoA), List.of(spotDtoValid, spotDtoMismatched));
    }

    public static RevenueResponseDto createMockRevenueResponseDto() {
        return new RevenueResponseDto(
                new BigDecimal("550.25"),
                "BRL",
                ZonedDateTime.of(2025, 10, 15, 18, 0, 0, 0, ZoneId.of("America/Sao_Paulo"))
        );
    }
}