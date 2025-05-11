package com.tanmoy.vpp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanmoy.vpp.dto.request.BatteryListRequest;
import com.tanmoy.vpp.dto.request.BatteryRequestDto;
import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BatteryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatteryRepository batteryRepository;

    @BeforeEach
    void setUp() {
        batteryRepository.deleteAll();
    }

    @Test
    void shouldSaveBatteriesAndReturnSuccess() throws Exception {

        List<BatteryRequestDto> batteries = List.of(
                createBatteryDto("Battery1", "6000", 1000),
                createBatteryDto("Battery2", "6001", 2000)
        );

        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(batteries);

        mockMvc.perform(post("/api/batteries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Saved 2 batteries successfully.")));

        List<Battery> savedBatteries = batteryRepository.findAll();
        assertThat(savedBatteries).hasSize(2);
        assertThat(savedBatteries).extracting("name")
                .containsExactlyInAnyOrder("Battery1", "Battery2");
    }

    @Test
    void shouldHandleInvalidPostcodeFormat() throws Exception {

        BatteryRequestDto battery = createBatteryDto("Battery1", "invalid-postcode", 1000);
        
        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(List.of(battery));

        mockMvc.perform(post("/api/batteries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['batteries[0].postcode']").exists());
    }

    @Test
    void shouldHandleLargeBatchOfBatteries() throws Exception {

        List<BatteryRequestDto> batteries = IntStream.range(0, 1000)
                .mapToObj(i -> {
                    BatteryRequestDto b = new BatteryRequestDto();
                    b.setName("Battery-" + i);
                    b.setPostcode("6000");
                    b.setCapacity(1000);
                    return b;
                }).collect(Collectors.toList());
        
        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(batteries);

        mockMvc.perform(post("/api/batteries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Saved 1000 batteries successfully.")));

        List<Battery> savedBatteries = batteryRepository.findAll();
        assertThat(savedBatteries).hasSize(1000);
    }

    @Test
    void shouldHandleZeroCapacity() throws Exception {

        BatteryRequestDto battery = createBatteryDto("Battery1", "6000", 0);
        
        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(List.of(battery));

        mockMvc.perform(post("/api/batteries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['batteries[0].capacity']").exists());
    }

    private BatteryRequestDto createBatteryDto(String name, String postcode, Integer capacity) {
        BatteryRequestDto dto = new BatteryRequestDto();
        dto.setName(name);
        dto.setPostcode(postcode);
        dto.setCapacity(capacity);
        return dto;
    }
} 