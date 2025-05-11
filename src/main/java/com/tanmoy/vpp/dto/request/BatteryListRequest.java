package com.tanmoy.vpp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BatteryListRequest {

    @NotEmpty
    private List<@Valid BatteryRequestDto> batteries;

    public BatteryListRequest() {}

    public void setBatteries(List<BatteryRequestDto> batteries) {
        this.batteries = batteries;
    }

    public List<BatteryRequestDto> getBatteries() {
        return batteries;
    }
}
