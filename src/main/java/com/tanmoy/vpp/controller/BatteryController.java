package com.tanmoy.vpp.controller;

import com.tanmoy.vpp.dto.request.BatteryRequestDto;
import com.tanmoy.vpp.dto.response.SuccessResponseDto;
import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.service.BatteryService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batteries")
public class BatteryController {

    private static final Logger logger = LogManager.getLogger(BatteryController.class);

    private final BatteryService batteryService;

    @Autowired
    public BatteryController(BatteryService batteryService) {
        this.batteryService = batteryService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto> insertBatteries(
            @RequestBody @Valid List<BatteryRequestDto> batteryRequests) {

        logger.info("Process insert batteries request: Size={}: START", batteryRequests.size());

        List<Battery> batteries = batteryRequests.stream()
                .map(req -> new Battery(req.getName(), req.getPostcode(), req.getCapacity()))
                .collect(Collectors.toList());

        batteryService.saveAll(batteries);

        logger.info("Process insert batteries request: Size={}: COMPLETE", batteryRequests.size());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponseDto("Saved " + batteryRequests.size() + " batteries successfully."));
    }
}
