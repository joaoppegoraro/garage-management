package br.com.garage_management.domain.mapper;

import br.com.garage_management.domain.dto.GarageSectorDto;
import br.com.garage_management.domain.model.GarageSector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GarageSectorMapper {

    @Mapping(target = "occupiedCount", constant = "0")
    GarageSector toEntity(GarageSectorDto dto);

}