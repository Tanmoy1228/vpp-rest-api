package com.tanmoy.vpp.dto.request;

import jakarta.validation.constraints.*;
import com.tanmoy.vpp.constant.ValidationMessages;

public class BatteryRequestDto {

    @NotBlank(message = ValidationMessages.BATTERY_NAME_REQUIRED)
    private String name;

    @NotBlank(message = ValidationMessages.POSTCODE_REQUIRED)
    @Size(min = 1, max = 10, message = ValidationMessages.POSTCODE_SIZE)
    private String postcode;

    @NotNull(message = ValidationMessages.CAPACITY_REQUIRED)
    @Positive(message = ValidationMessages.CAPACITY_POSITIVE)
    private Integer capacity;

    public BatteryRequestDto() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
