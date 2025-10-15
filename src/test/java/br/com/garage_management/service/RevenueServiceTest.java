package br.com.garage_management.service;

import br.com.garage_management.domain.dto.RevenueResponseDto;
import br.com.garage_management.repository.ParkingRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @InjectMocks
    private RevenueService revenueService;

    @Mock
    private ParkingRecordRepository recordRepository;

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 10, 15);
    private static final String TEST_SECTOR = "A";
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2025, 10, 15, 16, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));


    @Test
    @DisplayName("Deve retornar a receita correta quando o repositório encontra dados")
    void shouldReturnRevenueSuccesfully() {
        BigDecimal expectedRevenue = new BigDecimal("250.75");

        when(recordRepository.calculateTotalRevenueBySectorAndDate(TEST_SECTOR, TEST_DATE))
                .thenReturn(Optional.of(expectedRevenue));

        try (MockedStatic<ZonedDateTime> mockedStatic = Mockito.mockStatic(ZonedDateTime.class)) {
            mockedStatic.when(() -> ZonedDateTime.now(any(ZoneId.class))).thenReturn(TIMESTAMP);

            RevenueResponseDto response = revenueService.calculateDailyRevenue(TEST_DATE, TEST_SECTOR);

            assertNotNull(response);
        }
    }

    @Test
    @DisplayName("Deve retornar receita zero quando o repositório não encontra dados")
    void shouldReturnZeroRevenueWhenRepositoryFindsNoData() {
        when(recordRepository.calculateTotalRevenueBySectorAndDate(TEST_SECTOR, TEST_DATE))
                .thenReturn(Optional.empty());

        try (MockedStatic<ZonedDateTime> mockedStatic = Mockito.mockStatic(ZonedDateTime.class)) {
            mockedStatic.when(() -> ZonedDateTime.now(any(ZoneId.class))).thenReturn(TIMESTAMP);

            RevenueResponseDto response = revenueService.calculateDailyRevenue(TEST_DATE, TEST_SECTOR);

            assertNotNull(response);
        }
    }
}