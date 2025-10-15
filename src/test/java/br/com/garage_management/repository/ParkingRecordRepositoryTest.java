package br.com.garage_management.repository;

import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.model.ParkingSpace;
import br.com.garage_management.domain.enums.ParkingStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ParkingRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ParkingRecordRepository recordRepository;
    private GarageSector sectorA;
    private GarageSector sectorB;
    private ParkingSpace spaceA1;
    private ParkingSpace spaceB1;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        sectorA = createAndPersistSector("A", 10.0);
        sectorB = createAndPersistSector("B", 12.0);
        spaceA1 = createAndPersistSpace(sectorA);
        spaceB1 = createAndPersistSpace(sectorB);
        targetDate = LocalDate.of(2025, 10, 15);
    }

    @Test
    @DisplayName("Deve calcular a receita total para um setor e data específicos corretamente")
    void shouldCalculateTotalRevenueBySectorAndDate() {
        createAndPersistRecord("AAA-1111", sectorA, spaceA1, targetDate.atTime(14, 0), new BigDecimal("150.50"), ParkingStatusEnum.COMPLETED);
        createAndPersistRecord("BBB-2222", sectorA, spaceA1, targetDate.atTime(16, 0), new BigDecimal("50.00"), ParkingStatusEnum.COMPLETED);
        createAndPersistRecord("CCC-3333", sectorB, spaceB1, targetDate.atTime(15, 0), new BigDecimal("1000.00"), ParkingStatusEnum.COMPLETED);
        createAndPersistRecord("DDD-4444", sectorA, spaceA1, targetDate.plusDays(1).atTime(10, 0), new BigDecimal("2000.00"), ParkingStatusEnum.COMPLETED);
        createAndPersistRecord("EEE-5555", sectorA, spaceA1, null, null, ParkingStatusEnum.PARKED);

        entityManager.flush();

        Optional<BigDecimal> result = recordRepository.calculateTotalRevenueBySectorAndDate("A", targetDate);

        assertTrue(result.isPresent());
        BigDecimal expectedSum = new BigDecimal("200.50");
        assertEquals(0, expectedSum.compareTo(result.get()), "A soma da receita está incorreta.");
    }


    private GarageSector createAndPersistSector(String name, double basePrice) {
        GarageSector sector = new GarageSector(name, basePrice, 10, 0, LocalTime.MIN, LocalTime.MAX, 600, null, null);
        return entityManager.persist(sector);
    }

    private ParkingSpace createAndPersistSpace(GarageSector sector) {
        ParkingSpace space = new ParkingSpace(null, sector, false, 10.0, 20.0);
        return entityManager.persist(space);
    }

    private void createAndPersistRecord(String licensePlate, GarageSector sector, ParkingSpace space, LocalDateTime exitTime, BigDecimal finalPrice, ParkingStatusEnum status) {
        ParkingRecord record = new ParkingRecord(
                null,
                licensePlate,
                LocalDate.of(2025, 10, 15).atTime(10, 0),
                exitTime,
                sector,
                space,
                sector.getBasePrice(),
                finalPrice,
                status,
                null, null
        );
        entityManager.persist(record);
    }
}