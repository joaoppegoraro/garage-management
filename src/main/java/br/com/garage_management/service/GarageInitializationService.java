package br.com.garage_management.service;

import br.com.garage_management.domain.dto.GarageConfigDto;
import br.com.garage_management.domain.mapper.GarageSectorMapper;
import br.com.garage_management.domain.mapper.ParkingSpaceMapper;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.repository.GarageSectorRepository;
import br.com.garage_management.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GarageInitializationService implements ApplicationRunner {

    private static final String SIMULATOR_URL = "http://localhost:3000/garage";

    private final GarageSectorMapper garageSectorMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final RestTemplate restTemplate;
    private final GarageSectorRepository garageSectorRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("INICIANDO FEATURE: Integração com o Simulador...");

        if (garageSectorRepository.count() > 0 || parkingSpaceRepository.count() > 0) {
            log.warn("Banco de dados já populado. Pular integração inicial.");
            return;
        }

        try {
            log.info("Buscando configuração da garagem em: {}", SIMULATOR_URL);
            GarageConfigDto config = restTemplate.getForObject(SIMULATOR_URL, GarageConfigDto.class);

            if (config != null) {
                log.info("Salvando {} setores...", config.garage().size());
                Map<String, GarageSector> sectorsMap = config.garage().stream()
                        .map(garageSectorMapper::toEntity)
                        .map(garageSectorRepository::save)
                        .collect(Collectors.toMap(GarageSector::getSector, sector -> sector));

                log.info("Salvando {} vagas...", config.spots().size());
                config.spots().forEach(dto -> {
                    GarageSector correspondingSector = sectorsMap.get(dto.sector());
                    if (correspondingSector != null) {
                        var entity = parkingSpaceMapper.toEntity(dto, correspondingSector);
                        parkingSpaceRepository.save(entity);
                    } else {
                        log.warn("Vaga com setor '{}' não pôde ser salva pois o setor não foi encontrado no mapa.", dto.sector());
                    }
                });

                log.info("Configuração da garagem salva no banco de dados com sucesso!");
            }
        } catch (Exception e) {
            log.error("ERRO CRÍTICO: Falha ao buscar configuração do simulador. Verifique se ele está rodando.", e);
        }
    }
}