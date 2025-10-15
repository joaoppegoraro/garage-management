package br.com.garage_management.repository;

import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.enums.ParkingStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {

    Optional<ParkingRecord> findByLicensePlateAndStatus(String licensePlate, ParkingStatusEnum status);

    @Query("SELECT SUM(pr.finalPrice) FROM ParkingRecord pr WHERE pr.garageSector.sector = :sector AND CAST(pr.exitTime AS DATE) = :date AND pr.status = 'COMPLETED'")
    Optional<BigDecimal> calculateTotalRevenueBySectorAndDate(@Param("sector") String sector, @Param("date") LocalDate date);

    boolean existsByLicensePlateAndStatus(String licensePlate, ParkingStatusEnum status);
}