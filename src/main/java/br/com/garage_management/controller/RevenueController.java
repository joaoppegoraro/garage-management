package br.com.garage_management.controller;

import br.com.garage_management.domain.dto.RevenueResponseDto;
import br.com.garage_management.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public ResponseEntity<RevenueResponseDto> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam String sector) {

        RevenueResponseDto revenueResponseDto = revenueService.calculateDailyRevenue(date, sector);

        return ResponseEntity.ok(revenueResponseDto);
    }
}