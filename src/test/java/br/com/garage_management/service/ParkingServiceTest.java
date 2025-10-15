package br.com.garage_management.service;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.exception.BusinessException;
import br.com.garage_management.domain.mapper.ParkingRecordMapper;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.model.ParkingSpace;
import br.com.garage_management.domain.enums.ParkingStatusEnum;
import br.com.garage_management.repository.GarageSectorRepository;
import br.com.garage_management.repository.ParkingRecordRepository;
import br.com.garage_management.repository.ParkingSpaceRepository;
import br.com.garage_management.util.TestFactoryUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static br.com.garage_management.util.ErrorConstantsEnum.DUPLICATE_LICENSE_PLATE;
import static br.com.garage_management.util.ErrorConstantsEnum.INVALID_ENTRY_TIME;
import static br.com.garage_management.util.ErrorConstantsEnum.INVALID_EXIT_TIME;
import static br.com.garage_management.util.ErrorConstantsEnum.LICENSE_PLATE_NOT_FOUND;
import static br.com.garage_management.util.ErrorConstantsEnum.PARKING_SPACE_ALREADY_OCCUPIED;
import static br.com.garage_management.util.ErrorConstantsEnum.PARKING_SPACE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private ParkingRecordRepository parkingRecordRepository;

    @Mock
    private GarageSectorRepository garageSectorRepository;

    @Mock
    private ParkingSpaceRepository parkingSpaceRepository;

    @Mock
    private ParkingRecordMapper parkingRecordMapper;

    @Captor
    private ArgumentCaptor<ParkingRecord> parkingRecordCaptor;

    @Captor
    private ArgumentCaptor<ParkingSpace> parkingSpaceCaptor;

    @Captor
    private ArgumentCaptor<BigDecimal> priceCaptor;

    @Test
    @DisplayName("Deve lançar exceção para tempo de entrada no passado")
    void shouldThrowExceptionWhenEntryTimeIsInvalid() {
        EntryEventDto event = TestFactoryUtil.createEntryEventDto("ABC-1234", LocalDateTime.now().minusMinutes(1));

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processEntry(event));

        assertEquals(INVALID_ENTRY_TIME.getMessage(), thrownException.getMessage());

        verify(parkingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção para placa de veículo que já está estacionada")
    void shouldThrowExceptionWhenLicensePlateIsDuplicate() {
        EntryEventDto event = TestFactoryUtil.createEntryEventDto("ABC-1234", LocalDateTime.now().plusHours(1));

        when(parkingRecordRepository.existsByLicensePlateAndStatus(event.licensePlate(), ParkingStatusEnum.PARKED)).thenReturn(true);

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processEntry(event));

        assertEquals(DUPLICATE_LICENSE_PLATE.getMessage(), thrownException.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"24, 100, 10.00, 9.00", "49, 100, 10.00, 10.00", "74, 100, 10.00, 11.00", "75, 100, 10.00, 12.50"})
    @DisplayName("Deve aplicar o preço dinâmico correto com base na ocupação do setor")
    void shouldApplyCorrectDynamicPrice(int occupiedCount, int maxCapacity, double basePrice, double expectedPrice) {
        EntryEventDto event = TestFactoryUtil.createEntryEventDto("XYZ-5678", LocalDateTime.now().plusHours(1));

        when(parkingRecordRepository.existsByLicensePlateAndStatus(anyString(), any())).thenReturn(false);
        GarageSector sector = TestFactoryUtil.createMockGarageSector("A1", occupiedCount, maxCapacity, basePrice);
        ParkingSpace space = TestFactoryUtil.createMockParkingSpace(101L, false, sector);
        when(garageSectorRepository.findAll()).thenReturn(List.of(sector));
        when(parkingSpaceRepository.findFirstByGarageSectorAndIsOccupied(sector, false)).thenReturn(Optional.of(space));
        when(parkingRecordMapper.toParkingRecord(any(), any(), any(), anyDouble())).thenReturn(new ParkingRecord());

        parkingService.processEntry(event);

        ArgumentCaptor<Double> priceCaptor = ArgumentCaptor.forClass(Double.class);
        verify(parkingRecordMapper).toParkingRecord(eq(event), eq(sector), eq(space), priceCaptor.capture());
        assertEquals(expectedPrice, priceCaptor.getValue());
    }

    @Test
    @DisplayName("Deve processar a entrada com sucesso quando há vagas")
    void shouldProcessEntryWhenGarageHasSpaces() {
        LocalDateTime fixedTime = LocalDateTime.of(2025, 10, 15, 14, 0, 0);
        EntryEventDto event = TestFactoryUtil.createEntryEventDto("ABC-1234", fixedTime.plusHours(1));

        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedTime);
            when(parkingRecordRepository.existsByLicensePlateAndStatus(anyString(), any())).thenReturn(false);
            GarageSector sector = TestFactoryUtil.createMockGarageSector("A1", 50, 100, 10.0);
            ParkingSpace space = TestFactoryUtil.createMockParkingSpace(101L, false, sector);
            when(garageSectorRepository.findAll()).thenReturn(Collections.singletonList(sector));
            when(parkingSpaceRepository.findFirstByGarageSectorAndIsOccupied(sector, false)).thenReturn(Optional.of(space));
            when(parkingRecordMapper.toParkingRecord(any(), any(), any(), anyDouble())).thenReturn(new ParkingRecord());

            parkingService.processEntry(event);

            assertTrue(space.getIsOccupied());
            verify(parkingSpaceRepository).save(space);
            assertEquals(51, sector.getOccupiedCount());
            verify(garageSectorRepository).save(sector);
            verify(parkingRecordRepository).save(any(ParkingRecord.class));
        }
    }

    @Test
    @DisplayName("Deve lançar exceção se a placa do veículo não for encontrada")
    void shouldThrowExceptionWhenLicensePlateNotFound() {
        ParkedEventDto event = TestFactoryUtil.createParkedEventDto("NOT-FOUND");

        when(parkingRecordRepository.findByLicensePlateAndStatus(event.licensePlate(), ParkingStatusEnum.PARKED)).thenReturn(Optional.empty());

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processParked(event));

        assertEquals(LICENSE_PLATE_NOT_FOUND.getMessage(), thrownException.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se a vaga (lat/lng) não for encontrada")
    void shouldThrowExceptionWhenParkingSpaceNotFound() {
        ParkedEventDto event = TestFactoryUtil.createParkedEventDto("ABC-1234");

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(new ParkingRecord()));
        when(parkingSpaceRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.empty());

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processParked(event));

        assertEquals(PARKING_SPACE_NOT_FOUND.getMessage(), thrownException.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar estacionar em uma vaga diferente que já está ocupada")
    void shouldThrowExceptionWhenActualSpaceIsAlreadyOccupied() {
        ParkedEventDto event = TestFactoryUtil.createParkedEventDto("ABC-1234");
        ParkingSpace assignedSpace = TestFactoryUtil.createMockParkingSpace(1L, true, new GarageSector());
        ParkingSpace actualOccupiedSpace = TestFactoryUtil.createMockParkingSpace(2L, true, new GarageSector());
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(LocalDateTime.now(), 10.0, assignedSpace, new GarageSector());

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));
        when(parkingSpaceRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.of(actualOccupiedSpace));

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processParked(event));
        assertEquals(PARKING_SPACE_ALREADY_OCCUPIED.getMessage(), thrownException.getMessage());
    }

    @Test
    @DisplayName("Deve processar com sucesso quando o veículo estaciona na vaga designada")
    void shouldSucceedWhenVehicleParksInAssignedSpace() {
        ParkedEventDto event = TestFactoryUtil.createParkedEventDto("ABC-1234");
        ParkingSpace space = TestFactoryUtil.createMockParkingSpace(1L, true, new GarageSector());
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(LocalDateTime.now(), 10.0, space, new GarageSector());
        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));
        when(parkingSpaceRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.of(space));

        parkingService.processParked(event);

        verify(parkingSpaceRepository, never()).save(any());
        verify(parkingRecordMapper).updateRecordOnParked(record, event);
        verify(parkingRecordRepository).save(record);
    }

    @Test
    @DisplayName("Deve corrigir a vaga se o veículo estacionar em um local diferente, mas válido")
    void shouldCorrectSpaceWhenVehicleParksInDifferentValidSpace() {
        ParkedEventDto event = TestFactoryUtil.createParkedEventDto("ABC-1234");
        GarageSector originalSector = TestFactoryUtil.createMockGarageSector("A1", 1, 1, 10.0);
        GarageSector newSector = TestFactoryUtil.createMockGarageSector("B2", 0, 1, 12.0);
        ParkingSpace originalSpace = TestFactoryUtil.createMockParkingSpace(1L, true, originalSector);
        ParkingSpace actualSpace = TestFactoryUtil.createMockParkingSpace(2L, false, newSector);
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(LocalDateTime.now(), 10.0, originalSpace, originalSector);

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));
        when(parkingSpaceRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.of(actualSpace));

        parkingService.processParked(event);

        verify(parkingSpaceRepository, times(2)).save(parkingSpaceCaptor.capture());
        ParkingSpace freedSpace = parkingSpaceCaptor.getAllValues().get(0);
        ParkingSpace occupiedSpace = parkingSpaceCaptor.getAllValues().get(1);
        assertFalse(freedSpace.getIsOccupied());
        assertTrue(occupiedSpace.getIsOccupied());

        verify(parkingRecordRepository).save(parkingRecordCaptor.capture());
        ParkingRecord savedRecord = parkingRecordCaptor.getValue();
        assertEquals(actualSpace.getId(), savedRecord.getParkingSpace().getId());
        assertEquals(newSector.getSector(), savedRecord.getGarageSector().getSector());
    }

    @Test
    @DisplayName("Deve lançar exceção se a placa não for encontrada ou não estiver estacionada")
    void shouldThrowExceptionWhenLicensePlateNotFoundForExit() {
        ExitEventDto event = TestFactoryUtil.createExitEventDto("NOT-FOUND", LocalDateTime.now());

        when(parkingRecordRepository.findByLicensePlateAndStatus(event.licensePlate(), ParkingStatusEnum.PARKED)).thenReturn(Optional.empty());

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processExit(event));
        assertEquals(LICENSE_PLATE_NOT_FOUND.getMessage(), thrownException.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se o tempo de saída for anterior ao tempo de entrada")
    void shouldThrowExceptionWhenExitTimeIsInvalid() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 10, 15, 14, 0, 0);
        ExitEventDto event = TestFactoryUtil.createExitEventDto("ABC-1234", entryTime.minusMinutes(1));
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(entryTime, 10.0, new ParkingSpace(), new GarageSector());

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));

        var thrownException = assertThrows(BusinessException.class, () -> parkingService.processExit(event));
        assertEquals(INVALID_EXIT_TIME.getMessage(), thrownException.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"29, 10.0, 0.00", "60, 10.0, 10.00", "75, 10.0, 20.00", "121, 15.5, 46.50"})
    @DisplayName("Deve calcular o preço final corretamente com base no tempo de permanência")
    void shouldCalculateFinalPriceCorrectly(long minutesStayed, double priceOnEntry, double expectedFinalPrice) {
        LocalDateTime entryTime = LocalDateTime.now();
        ExitEventDto event = TestFactoryUtil.createExitEventDto("ABC-1234", entryTime.plusMinutes(minutesStayed));
        GarageSector sector = TestFactoryUtil.createMockGarageSector("A1", 10, 20, 10.0);
        ParkingSpace space = TestFactoryUtil.createMockParkingSpace(1L, true, sector);
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(entryTime, priceOnEntry, space, sector);

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));

        parkingService.processExit(event);

        verify(parkingRecordMapper).updateRecordOnExit(any(), any(), priceCaptor.capture());
        assertEquals(0, BigDecimal.valueOf(expectedFinalPrice).compareTo(priceCaptor.getValue()), "O preço final calculado está incorreto.");
    }

    @Test
    @DisplayName("Deve processar a saída com sucesso e liberar a vaga e o setor")
    void shouldProcessExitAndReleaseTheVacancyAndSector() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 10, 15, 14, 0, 0);
        ExitEventDto event = TestFactoryUtil.createExitEventDto("ABC-1234", entryTime.plusHours(2));
        GarageSector sector = TestFactoryUtil.createMockGarageSector("A1", 50, 100, 10.0);
        ParkingSpace space = TestFactoryUtil.createMockParkingSpace(101L, true, sector);
        ParkingRecord record = TestFactoryUtil.createMockParkingRecord(entryTime, 12.0, space, sector);

        when(parkingRecordRepository.findByLicensePlateAndStatus(anyString(), any())).thenReturn(Optional.of(record));

        parkingService.processExit(event);

        verify(parkingRecordMapper).updateRecordOnExit(parkingRecordCaptor.capture(), eq(event), priceCaptor.capture());
        assertEquals(0, BigDecimal.valueOf(24.0).compareTo(priceCaptor.getValue()));
        verify(parkingRecordRepository).save(parkingRecordCaptor.getValue());
        assertFalse(space.getIsOccupied());
        verify(parkingSpaceRepository).save(space);
        assertEquals(49, sector.getOccupiedCount());
        verify(garageSectorRepository).save(sector);
    }
}