package com.tanmoy.vpp.service;

import com.tanmoy.vpp.model.Battery;

import java.util.List;

public interface BatteryService {

    void saveAll(List<Battery> batteries);
}
