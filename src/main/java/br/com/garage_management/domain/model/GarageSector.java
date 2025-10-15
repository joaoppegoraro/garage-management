package br.com.garage_management.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_garage_sectors")
public class GarageSector {

    @Id
    @Column(name = "sector")
    private String sector;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "occupied_count", nullable = false)
    private Integer occupiedCount;

    @Column(name = "open_hour", nullable = false)
    private LocalTime openHour;

    @Column(name = "close_hour", nullable = false)
    private LocalTime closeHour;

    @Column(name = "duration_limit_minutes", nullable = false)
    private Integer durationLimitMinutes;

    @OneToMany(mappedBy = "garageSector")
    private List<ParkingSpace> parkingSpaces;

    @OneToMany(mappedBy = "garageSector")
    private List<ParkingRecord> parkingRecords;
}