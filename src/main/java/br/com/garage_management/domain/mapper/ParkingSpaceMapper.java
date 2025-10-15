package br.com.garage_management.domain.mapper;

import br.com.garage_management.domain.dto.ParkingSpotDto;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingSpace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParkingSpaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isOccupied", constant = "false")
    @Mapping(source = "sectorEntity", target = "garageSector")
    ParkingSpace toEntity(ParkingSpotDto dto, GarageSector sectorEntity);

}