package com.tanmoy.vpp.service.impl;

import com.tanmoy.vpp.dto.response.BatterySearchResponseDto;
import com.tanmoy.vpp.exception.InvalidRangeException;
import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.repository.BatteryRepository;
import com.tanmoy.vpp.service.BatteryService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatteryServiceImpl implements BatteryService {

    private static final Logger logger = LogManager.getLogger(BatteryServiceImpl.class);

    private final BatteryRepository batteryRepository;

    @Autowired
    public BatteryServiceImpl(BatteryRepository batteryRepository) {
        this.batteryRepository = batteryRepository;
    }

    @Override
    @Transactional
    public void saveAll(List<Battery> batteries) {

        logger.info("Saving batteries: Size={}: START", batteries.size());

        batteryRepository.saveAll(batteries);

        logger.info("Saving batteries: Size={}: COMPLETE", batteries.size());
    }

    public BatterySearchResponseDto getBatteriesByPostcodeRange(
            int startPostcode, int endPostcode, Integer minCapacity, Integer maxCapacity) {

        logger.info("Search batteries: StartPostcode={}, EndPostcode={}: START", startPostcode, endPostcode);

        if (startPostcode > endPostcode) {
            throw new InvalidRangeException("Start postcode must be less than or equal to end postcode");
        }

        List<Battery> batteries = batteryRepository.findInRangeWithOptionalCapacity(
                startPostcode, endPostcode, minCapacity, maxCapacity);

        List<String> names = batteries.stream()
                .map(Battery::getName)
                .sorted()
                .collect(Collectors.toList());

        long totalCapacity = batteries.stream().mapToLong(Battery::getCapacity).sum();
        double averageCapacity = batteries.isEmpty() ? 0.0 : (double) totalCapacity / batteries.size();

        logger.info("Search batteries: StartPostcode={}, EndPostcode={}: COMPLETE", startPostcode, endPostcode);

        return new BatterySearchResponseDto(names, totalCapacity, averageCapacity);
    }

}
