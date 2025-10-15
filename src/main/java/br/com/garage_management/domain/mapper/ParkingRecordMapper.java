package br.com.garage_management.domain.mapper;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.model.ParkingSpace;
import br.com.garage_management.domain.enums.ParkingStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = { ParkingStatusEnum.class }
)public interface ParkingRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exitTime", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(source = "event.licensePlate", target = "licensePlate")
    @Mapping(source = "event.entryTime", target = "entryTime")
    @Mapping(source = "sector", target = "garageSector")
    @Mapping(source = "space", target = "parkingSpace")
    @Mapping(source = "price", target = "priceAppliedOnEntry")
    @Mapping(target = "status", expression = "java(ParkingStatusEnum.PARKED)")
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lng", ignore = true)
    ParkingRecord toParkingRecord(EntryEventDto event, GarageSector sector, ParkingSpace space, Double price);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "event.lat", target = "lat")
    @Mapping(source = "event.lng", target = "lng")
    @Mapping(target = "licensePlate", ignore = true)
    @Mapping(target = "entryTime", ignore = true)
    @Mapping(target = "exitTime", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priceAppliedOnEntry", ignore = true)
    @Mapping(target = "garageSector", ignore = true)
    @Mapping(target = "parkingSpace", ignore = true)
    void updateRecordOnParked(@MappingTarget ParkingRecord record, ParkedEventDto event);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "event.exitTime", target = "exitTime")
    @Mapping(source = "finalPrice", target = "finalPrice")
    @Mapping(target = "status", expression = "java(ParkingStatusEnum.COMPLETED)")
    @Mapping(target = "licensePlate", ignore = true)
    @Mapping(target = "entryTime", ignore = true)
    @Mapping(target = "garageSector", ignore = true)
    @Mapping(target = "parkingSpace", ignore = true)
    @Mapping(target = "priceAppliedOnEntry", ignore = true)
    @Mapping(target = "lat", ignore = true)
    @Mapping(target = "lng", ignore = true)
    void updateRecordOnExit(@MappingTarget ParkingRecord record, ExitEventDto event, BigDecimal finalPrice);
}