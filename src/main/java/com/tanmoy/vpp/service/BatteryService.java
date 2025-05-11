package com.tanmoy.vpp.service;

import com.tanmoy.vpp.dto.response.BatterySearchResponseDto;
import com.tanmoy.vpp.model.Battery;

import java.util.List;

public interface BatteryService {

    void saveAll(List<Battery> batteries);

    BatterySearchResponseDto getBatteriesByPostcodeRange(
            int startPostcode, int endPostcode, Integer minCapacity, Integer maxCapacity);
}
