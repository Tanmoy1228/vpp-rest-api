package com.tanmoy.vpp.repository;

import com.tanmoy.vpp.model.Battery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BatteryRepositoryTest {

    @Autowired
    private BatteryRepository batteryRepository;

    @BeforeEach
    void setup() {
        batteryRepository.saveAll(List.of(
                new Battery("Alpha", "6000", 1000),
                new Battery("Beta", "6001", 2000),
                new Battery("Gamma", "6002", 3000)
        ));
    }

    @Test
    void shouldPersistBatteryEntity() {

        Battery battery = new Battery("Battery", "1234", 3000);
        Battery savedBattery = batteryRepository.save(battery);

        assertThat(savedBattery).isNotNull();
        assertThat(savedBattery.getId()).isNotNull();
        assertThat(savedBattery.getName()).isEqualTo(battery.getName());
        assertThat(savedBattery.getPostcode()).isEqualTo(battery.getPostcode());
        assertThat(savedBattery.getCapacity()).isEqualTo(battery.getCapacity());
    }

    @Test
    void shouldSaveAllBatteries() {

        List<Battery> batteries = List.of(
                new Battery("Battery1", "6000", 1000),
                new Battery("Battery2", "6001", 2000),
                new Battery("Battery3", "6002", 3000)
        );

        List<Battery> savedBatteries = batteryRepository.saveAll(batteries);

        assertThat(savedBatteries).hasSize(3);
        assertThat(savedBatteries).extracting("name")
                .containsExactlyInAnyOrder("Battery1", "Battery2", "Battery3");
    }

    @Test
    void shouldFindBatteryById() {

        Battery battery = new Battery("Battery", "1234", 3000);

        Battery savedBattery = batteryRepository.save(battery);
        Optional<Battery> foundBattery = batteryRepository.findById(savedBattery.getId());

        assertThat(foundBattery).isPresent();
        assertThat(foundBattery.get().getName()).isEqualTo(battery.getName());
    }

    @Test
    void shouldFindBatteriesWithinPostcodeRange() {
        List<Battery> results = batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6002, null, null);
        assertThat(results).hasSize(3);
    }

    @Test
    void shouldFilterByMinCapacity() {
        List<Battery> results = batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6002, 2000, null);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Beta", "Gamma");
    }

    @Test
    void shouldFilterByMaxCapacity() {
        List<Battery> results = batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6002, null, 2000);
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Alpha", "Beta");
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        List<Battery> results = batteryRepository.findInRangeWithOptionalCapacity(
                7000, 8000, null, null);
        assertThat(results).isEmpty();
    }
}