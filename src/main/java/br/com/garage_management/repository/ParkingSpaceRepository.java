package br.com.garage_management.repository;

import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    Optional<ParkingSpace> findFirstByGarageSectorAndIsOccupied(GarageSector garageSector, boolean isOccupied);

    Optional<ParkingSpace> findByLatAndLng(Double lat, Double lng);

}