package br.com.garage_management.service;

import br.com.garage_management.domain.dto.RevenueResponseDto;
import br.com.garage_management.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class RevenueService {

    private final ParkingRecordRepository recordRepository;

    public RevenueResponseDto calculateDailyRevenue(LocalDate date, String sector) {

        BigDecimal totalAmount = recordRepository
                .calculateTotalRevenueBySectorAndDate(sector, date)
                .orElse(BigDecimal.valueOf(0.00));

        return new RevenueResponseDto(
                totalAmount, "BRL", ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")));
    }
}