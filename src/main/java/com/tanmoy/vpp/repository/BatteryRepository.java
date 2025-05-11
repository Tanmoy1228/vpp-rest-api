package com.tanmoy.vpp.repository;

import com.tanmoy.vpp.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatteryRepository extends JpaRepository<Battery, UUID> {

}
