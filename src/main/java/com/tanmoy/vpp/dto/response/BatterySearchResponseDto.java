package com.tanmoy.vpp.dto.response;

import java.util.List;

public class BatterySearchResponseDto {

    private List<String> batteryNames;
    private long totalWattCapacity;
    private double averageWattCapacity;

    public BatterySearchResponseDto(List<String> batteryNames, long totalWattCapacity, double averageWattCapacity) {
        this.batteryNames = batteryNames;
        this.totalWattCapacity = totalWattCapacity;
        this.averageWattCapacity = averageWattCapacity;
    }

    public List<String> getBatteryNames() {
        return batteryNames;
    }

    public void setBatteryNames(List<String> batteryNames) {
        this.batteryNames = batteryNames;
    }

    public long getTotalWattCapacity() {
        return totalWattCapacity;
    }

    public void setTotalWattCapacity(long totalWattCapacity) {
        this.totalWattCapacity = totalWattCapacity;
    }

    public double getAverageWattCapacity() {
        return averageWattCapacity;
    }

    public void setAverageWattCapacity(double averageWattCapacity) {
        this.averageWattCapacity = averageWattCapacity;
    }
}
