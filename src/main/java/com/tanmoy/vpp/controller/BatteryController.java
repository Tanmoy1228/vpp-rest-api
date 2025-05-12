package com.tanmoy.vpp.controller;

import com.tanmoy.vpp.dto.request.BatteryListRequest;
import com.tanmoy.vpp.dto.response.BatterySearchResponseDto;
import com.tanmoy.vpp.dto.response.SuccessResponseDto;
import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.service.BatteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Insert a list of batteries")
    @PostMapping
    public ResponseEntity<SuccessResponseDto> insertBatteries(
            @RequestBody @Valid BatteryListRequest batteryListRequest) {

        logger.info("Process insert batteries request: Size={}: START", batteryListRequest.getBatteries().size());

        List<Battery> batteries = batteryListRequest.getBatteries().stream()
                .map(req -> Battery.of(req.getName(), req.getPostcode(), req.getCapacity()))
                .collect(Collectors.toList());

        batteryService.saveAll(batteries);

        logger.info("Process insert batteries request: Size={}: COMPLETE", batteryListRequest.getBatteries().size());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponseDto("Saved " + batteryListRequest.getBatteries().size() + " batteries successfully."));
    }

    @Operation(summary = "Search batteries by postcode range")
    @GetMapping("/search")
    public ResponseEntity<BatterySearchResponseDto> getBatteriesByPostcodeRange(
            @Parameter(description = "Start of postcode range") @RequestParam int startPostcode,
            @Parameter(description = "End of postcode range") @RequestParam int endPostcode,
            @Parameter(description = "Minimum capacity of battery") @RequestParam(required = false) Integer minCapacity,
            @Parameter(description = "Maximum capacity of battery") @RequestParam(required = false) Integer maxCapacity) {

        logger.info("Process search batteries request: " +
                "StartPostcode={}, EndPostcode={}: START", startPostcode, endPostcode);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                startPostcode, endPostcode, minCapacity, maxCapacity);

        logger.info("Process search batteries request: " +
                "StartPostcode={}, EndPostcode={}: COMPLETE", startPostcode, endPostcode);

        return ResponseEntity.ok(response);
    }

}
