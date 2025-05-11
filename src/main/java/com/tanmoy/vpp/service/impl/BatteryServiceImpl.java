package com.tanmoy.vpp.service.impl;

import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.repository.BatteryRepository;
import com.tanmoy.vpp.service.BatteryService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
