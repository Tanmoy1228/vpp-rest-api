package com.tanmoy.vpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanmoy.vpp.dto.request.BatteryListRequest;
import com.tanmoy.vpp.dto.request.BatteryRequestDto;
import com.tanmoy.vpp.dto.response.BatterySearchResponseDto;
import com.tanmoy.vpp.exception.InvalidRangeException;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void shouldReturnBatteryStatsWhenValidRangeProvided() throws Exception {

        List<String> names = List.of("Alpha", "Beta");
        BatterySearchResponseDto mockResponse = new BatterySearchResponseDto(names, 3000L, 1500.0);

        when(batteryService.getBatteriesByPostcodeRange(
                6000, 6002, null, null)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "6000")
                        .param("endPostcode", "6002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryNames").isArray())
                .andExpect(jsonPath("$.batteryNames[0]").value("Alpha"))
                .andExpect(jsonPath("$.totalWattCapacity").value(3000))
                .andExpect(jsonPath("$.averageWattCapacity").value(1500.0));
    }

    @Test
    void shouldReturnBadRequestForInvalidPostcodeParam() throws Exception {

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "abc")
                        .param("endPostcode", "6002"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnErrorForInvalidRange() throws Exception {

        when(batteryService.getBatteriesByPostcodeRange(6002, 6000, null, null))
                .thenThrow(new InvalidRangeException("Start postcode must be less than or equal to end postcode"));

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "6002")
                        .param("endPostcode", "6000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start postcode must be less than or equal to end postcode"));
    }

    @Test
    void shouldReturnEmptyListIfNoMatches() throws Exception {

        BatterySearchResponseDto response = new BatterySearchResponseDto(
                Collections.emptyList(), 0L, 0.0);

        when(batteryService.getBatteriesByPostcodeRange(
                9000, 9001, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "9000")
                        .param("endPostcode", "9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryNames").isEmpty())
                .andExpect(jsonPath("$.totalWattCapacity").value(0))
                .andExpect(jsonPath("$.averageWattCapacity").value(0.0));
    }
}
