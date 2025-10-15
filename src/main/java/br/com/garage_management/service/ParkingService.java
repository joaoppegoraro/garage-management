package br.com.garage_management.service;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.domain.mapper.ParkingRecordMapper;
import br.com.garage_management.domain.model.GarageSector;
import br.com.garage_management.domain.model.ParkingRecord;
import br.com.garage_management.domain.model.ParkingSpace;
import br.com.garage_management.domain.enums.ParkingStatusEnum;
import br.com.garage_management.repository.GarageSectorRepository;
import br.com.garage_management.repository.ParkingRecordRepository;
import br.com.garage_management.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static br.com.garage_management.util.ErrorConstantsEnum.DUPLICATE_LICENSE_PLATE;
import static br.com.garage_management.util.ErrorConstantsEnum.GARAGE_WITHOUT_SPACES;
import static br.com.garage_management.util.ErrorConstantsEnum.INCONSISTENT_DATA_SPOTS_NOT_FOUND;
import static br.com.garage_management.util.ErrorConstantsEnum.INVALID_ENTRY_TIME;
import static br.com.garage_management.util.ErrorConstantsEnum.INVALID_EXIT_TIME;
import static br.com.garage_management.util.ErrorConstantsEnum.LICENSE_PLATE_NOT_FOUND;
import static br.com.garage_management.util.ErrorConstantsEnum.PARKING_SPACE_ALREADY_OCCUPIED;
import static br.com.garage_management.util.ErrorConstantsEnum.PARKING_SPACE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final GarageSectorRepository garageSectorRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private final ParkingRecordMapper parkingRecordMapper;

    @Transactional
    public void processEntry(EntryEventDto event) {

        if (event.entryTime().isBefore(LocalDateTime.now())) {
            throw INVALID_ENTRY_TIME.asException(String.valueOf(event.entryTime()));
        }

        boolean vehicleAlreadyParked = parkingRecordRepository.existsByLicensePlateAndStatus(
                event.licensePlate(), ParkingStatusEnum.PARKED);

        if (vehicleAlreadyParked) {
            throw DUPLICATE_LICENSE_PLATE.asException(event.licensePlate() );
        }

        GarageSector availableSector = garageSectorRepository.findAll().stream()
                .filter(sector -> sector.getOccupiedCount() < sector.getMaxCapacity())
                .findFirst()
                .orElseThrow(() -> GARAGE_WITHOUT_SPACES.asException(event.licensePlate()));

        ParkingSpace availableSpace = parkingSpaceRepository.findFirstByGarageSectorAndIsOccupied(availableSector, false)
                .orElseThrow(INCONSISTENT_DATA_SPOTS_NOT_FOUND::asException);

        double priceAppliedOnEntry = calculateDynamicPrice(availableSector);

        availableSpace.setIsOccupied(true);
        parkingSpaceRepository.save(availableSpace);

        availableSector.setOccupiedCount(availableSector.getOccupiedCount() + 1);
        garageSectorRepository.save(availableSector);

        ParkingRecord newRecord = parkingRecordMapper.toParkingRecord(event, availableSector, availableSpace, priceAppliedOnEntry);

        parkingRecordRepository.save(newRecord);
    }

    @Transactional
    public void processParked(ParkedEventDto event) {
        ParkingRecord record = parkingRecordRepository.findByLicensePlateAndStatus(event.licensePlate(), ParkingStatusEnum.PARKED)
                .orElseThrow(() -> LICENSE_PLATE_NOT_FOUND.asException(event.licensePlate()));

        ParkingSpace actualOccupiedSpace = parkingSpaceRepository.findByLatAndLng(event.lat(), event.lng())
                .orElseThrow(PARKING_SPACE_NOT_FOUND::asException);

        ParkingSpace assignedSpace = record.getParkingSpace();

        if (actualOccupiedSpace.getIsOccupied() && !actualOccupiedSpace.getId().equals(assignedSpace.getId())) {
            throw PARKING_SPACE_ALREADY_OCCUPIED.asException();
        }

        if (!actualOccupiedSpace.getId().equals(assignedSpace.getId())) {
            assignedSpace.setIsOccupied(false);
            parkingSpaceRepository.save(assignedSpace);

            actualOccupiedSpace.setIsOccupied(true);
            parkingSpaceRepository.save(actualOccupiedSpace);

            record.setParkingSpace(actualOccupiedSpace);
            record.setGarageSector(actualOccupiedSpace.getGarageSector());
        }

        parkingRecordMapper.updateRecordOnParked(record, event);
        parkingRecordRepository.save(record);
    }

    @Transactional
    public void processExit(ExitEventDto event) {
        ParkingRecord record = parkingRecordRepository.findByLicensePlateAndStatus(event.licensePlate(), ParkingStatusEnum.PARKED)
                .orElseThrow(() -> LICENSE_PLATE_NOT_FOUND.asException(event.licensePlate()));

        if (event.exitTime().isBefore(record.getEntryTime())) {
            throw INVALID_EXIT_TIME.asException();
        }

        ParkingSpace parkingSpace = record.getParkingSpace();
        GarageSector sector = record.getGarageSector();

        Duration duration = Duration.between(record.getEntryTime(), event.exitTime());
        long minutes = duration.toMinutes();
        BigDecimal finalPrice = BigDecimal.ZERO;

        if (minutes > 30) {
            double hours = Math.ceil((double) minutes / 60.0);
            finalPrice = BigDecimal.valueOf(hours * record.getPriceAppliedOnEntry());
        }

        parkingRecordMapper.updateRecordOnExit(record, event, finalPrice);
        parkingRecordRepository.save(record);

        parkingSpace.setIsOccupied(false);
        parkingSpaceRepository.save(parkingSpace);

        sector.setOccupiedCount(sector.getOccupiedCount() - 1);
        garageSectorRepository.save(sector);
    }

    private double calculateDynamicPrice(GarageSector sector) {
        double occupancyPercentage = (double) sector.getOccupiedCount() / sector.getMaxCapacity();
        double basePrice = sector.getBasePrice();

        if (occupancyPercentage < 0.25) return basePrice * 0.90;
        if (occupancyPercentage < 0.50) return basePrice;
        if (occupancyPercentage < 0.75) return basePrice * 1.10;
        return basePrice * 1.25;
    }
}