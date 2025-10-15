package br.com.garage_management.domain.model;

import br.com.garage_management.domain.enums.ParkingStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_parking_records")
public class ParkingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    private GarageSector garageSector;

    @ManyToOne
    @JoinColumn(name = "parking_space_id", nullable = false)
    private ParkingSpace parkingSpace;

    @Column(name = "price_applied_on_entry")
    private Double priceAppliedOnEntry;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParkingStatusEnum status;

    @Column(name = "latitude")
    private Double lat;

    @Column(name = "longitude")
    private Double lng;
}