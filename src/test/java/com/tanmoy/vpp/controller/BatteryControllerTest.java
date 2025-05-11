package com.tanmoy.vpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanmoy.vpp.dto.request.BatteryListRequest;
import com.tanmoy.vpp.dto.request.BatteryRequestDto;
import com.tanmoy.vpp.service.BatteryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatteryController.class)
public class BatteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatteryService batteryService;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("invalidBatteryInputs")
    void shouldReturnBadRequestForInvalidBatteries(BatteryRequestDto battery, String expectedField, String expectedMessage) throws Exception {
        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(List.of(battery));

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['batteries[0]." + expectedField + "']").value(expectedMessage));
    }

    static Stream<Arguments> invalidBatteryInputs() {
        return Stream.of(
                Arguments.of(newBattery(null, "6000", 1000), "name", "Battery name is required"),
                Arguments.of(newBattery("Battery", null, 1000), "postcode", "Postcode is required"),
                Arguments.of(newBattery("Battery", "6000", null), "capacity", "Capacity is required"),
                Arguments.of(newBattery("Battery", "01234567890", 1000), "postcode", "Postcode must be between 1 and 10 characters"),
                Arguments.of(newBattery("Battery", "6000", -1), "capacity", "Capacity must be a positive number")
        );
    }

    private static BatteryRequestDto newBattery(String name, String postcode, Integer capacity) {
        BatteryRequestDto dto = new BatteryRequestDto();
        dto.setName(name);
        dto.setPostcode(postcode);
        dto.setCapacity(capacity);
        return dto;
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        String badJson = "[{ \"name\": \"Battery\", \"postcode\": \"6000\", \"capacity\": \"oops\" }]";

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload format"));
    }

    @Test
    void shouldInsertBatteriesSuccessfully() throws Exception {
        BatteryRequestDto battery = new BatteryRequestDto();
        battery.setName("ValidBattery");
        battery.setPostcode("6000");
        battery.setCapacity(1000);

        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(List.of(battery));

        Mockito.doNothing().when(batteryService).saveAll(anyList());

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Saved 1 batteries successfully."));
    }

    @Test
    void shouldReturnBadRequestWhenBatteryListIsEmpty() throws Exception {
        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(Collections.emptyList());

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.batteries").exists());
    }

    @Test
    void shouldReturnBadRequestWhenBatteryListIsNull() throws Exception {
        String nullListJson = "{\"batteries\": null}";

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullListJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldInsertLargeNumberOfBatteriesQuickly() throws Exception {
        List<BatteryRequestDto> batteryList = IntStream.range(0, 1000)
                .mapToObj(i -> {
                    BatteryRequestDto b = new BatteryRequestDto();
                    b.setName("Battery-" + i);
                    b.setPostcode("6000");
                    b.setCapacity(1000);
                    return b;
                }).collect(Collectors.toList());

        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(batteryList);

        long start = System.currentTimeMillis();

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Saved 1000 batteries successfully."));

        long duration = System.currentTimeMillis() - start;
        assert duration < 5000 : "Bulk insert took too long: " + duration + "ms";
    }
}
