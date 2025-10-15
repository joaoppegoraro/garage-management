package br.com.garage_management.service;

import br.com.garage_management.domain.dto.GarageConfigDto;
import br.com.garage_management.domain.dto.GarageSectorDto;
import br.com.garage_management.domain.mapper.GarageSectorMapper;
import br.com.garage_management.domain.mapper.ParkingSpaceMapper;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingSpace;
import br.com.garage_management.repository.GarageSectorRepository;
import br.com.garage_management.repository.ParkingSpaceRepository;
import br.com.garage_management.util.TestFactoryUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarageInitializationServiceTest {

    private static final String SIMULATOR_URL = "http://localhost:3000/garage";

    @InjectMocks
    private GarageInitializationService garageInitializationService;

    @Mock
    private GarageSectorMapper garageSectorMapper;

    @Mock
    private ParkingSpaceMapper parkingSpaceMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GarageSectorRepository garageSectorRepository;

    @Mock
    private ParkingSpaceRepository parkingSpaceRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    private void setupSimulatorResponse(GarageConfigDto config) {
        when(restTemplate.getForObject(SIMULATOR_URL, GarageConfigDto.class)).thenReturn(config);

        when(garageSectorRepository.save(any(GarageSector.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(garageSectorMapper.toEntity(any(GarageSectorDto.class))).thenAnswer(invocation -> {
            GarageSectorDto inputDto = invocation.getArgument(0);

            GarageSector entity = new GarageSector();
            entity.setSector(inputDto.sector());
            return entity;
        });

        when(parkingSpaceMapper.toEntity(any(), any())).thenReturn(new ParkingSpace());
    }

    @Test
    @DisplayName("Deve buscar configuração e salvar entidades com sucesso quando o banco está vazio")
    void shouldFetchConfigAndSaveEntitiesWhenDatabaseIsEmpty() {
        when(garageSectorRepository.count()).thenReturn(0L);
        when(parkingSpaceRepository.count()).thenReturn(0L);
        GarageConfigDto config = TestFactoryUtil.createValidGarageConfigDto();
        setupSimulatorResponse(config);

        garageInitializationService.run(applicationArguments);

        verify(garageSectorRepository, times(2)).save(any(GarageSector.class));
        verify(parkingSpaceRepository, times(2)).save(any(ParkingSpace.class));
    }

    @Test
    @DisplayName("Deve pular a execução se o banco de dados já tiver dados")
    void shouldSkipExecutionWhenDatabaseIsAlreadyPopulated() {
        when(garageSectorRepository.count()).thenReturn(10L);

        garageInitializationService.run(applicationArguments);

        verify(restTemplate, never()).getForObject(anyString(), any());
        verify(garageSectorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar apenas vagas com setores válidos")
    void shouldSaveOnlySpotsWithValidSectorsWhenDataIsMismatched() {
        when(garageSectorRepository.count()).thenReturn(0L);
        when(parkingSpaceRepository.count()).thenReturn(0L);
        GarageConfigDto config = TestFactoryUtil.createMismatchedGarageConfigDto();
        setupSimulatorResponse(config);

        garageInitializationService.run(applicationArguments);

        verify(garageSectorRepository, times(1)).save(any(GarageSector.class));
        verify(parkingSpaceRepository, times(1)).save(any(ParkingSpace.class));
    }

    @Test
    @DisplayName("Deve capturar exceção e não quebrar a aplicação quando o simulador estiver offline")
    void shouldHandleExceptionWhenSimulatorIsDown() {
        when(garageSectorRepository.count()).thenReturn(0L);
        when(parkingSpaceRepository.count()).thenReturn(0L);
        when(restTemplate.getForObject(SIMULATOR_URL, GarageConfigDto.class))
                .thenThrow(new RestClientException("Simulador offline"));

        assertDoesNotThrow(() -> garageInitializationService.run(applicationArguments));
        verify(garageSectorRepository, never()).save(any());
    }
}