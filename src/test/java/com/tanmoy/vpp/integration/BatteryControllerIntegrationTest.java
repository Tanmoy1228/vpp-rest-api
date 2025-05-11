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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private void saveDefaultTestBatteries() {
        batteryRepository.saveAll(List.of(
                Battery.of("Alpha", "6000", 1000),
                Battery.of("Beta", "6001", 2000),
                Battery.of("Gamma", "6002", 3000),
                Battery.of("Delta", "6003", 4000)
        ));
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

    @Test
    void shouldHandleConcurrentRequests() throws Exception {

        int numberOfConcurrentRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentRequests);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<ResultActions>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            int index = i;
            futures.add(executorService.submit(() -> {
                try {
                    BatteryRequestDto dto = createBatteryDto(
                            "ConcurrentBattery-" + index, "600" + (index % 10), 1000 + index);

                    BatteryListRequest request = new BatteryListRequest();
                    request.setBatteries(List.of(dto));

                    String json = objectMapper.writeValueAsString(request);

                    ResultActions result = mockMvc.perform(post("/api/batteries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json));

                    latch.countDown();
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        for (Future<ResultActions> future : futures) {
            future.get().andExpect(status().isCreated());
        }

        List<Battery> saved = batteryRepository.findAll();
        assertThat(saved).hasSize(numberOfConcurrentRequests);
    }

    private BatteryRequestDto createBatteryDto(String name, String postcode, Integer capacity) {
        BatteryRequestDto dto = new BatteryRequestDto();
        dto.setName(name);
        dto.setPostcode(postcode);
        dto.setCapacity(capacity);
        return dto;
    }

    @Test
    void shouldReturnFilteredBatteriesAndStatsFromRealDB() throws Exception {

        saveDefaultTestBatteries();

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "6000")
                        .param("endPostcode", "6002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryNames").isArray())
                .andExpect(jsonPath("$.batteryNames[0]").value("Alpha"))
                .andExpect(jsonPath("$.batteryNames[1]").value("Beta"))
                .andExpect(jsonPath("$.batteryNames[2]").value("Gamma"))
                .andExpect(jsonPath("$.totalWattCapacity").value(6000))
                .andExpect(jsonPath("$.averageWattCapacity").value(2000.0));
    }

    @Test
    void shouldApplyMinCapacityFilterInIntegrationTest() throws Exception {

        saveDefaultTestBatteries();

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "6000")
                        .param("endPostcode", "6003")
                        .param("minCapacity", "3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryNames").isArray())
                .andExpect(jsonPath("$.batteryNames[0]").value("Delta"))
                .andExpect(jsonPath("$.batteryNames[1]").value("Gamma"))
                .andExpect(jsonPath("$.totalWattCapacity").value(7000))
                .andExpect(jsonPath("$.averageWattCapacity").value(3500.0));
    }

    @Test
    void shouldReturnBadRequestForInvalidPostcodeInIntegration() throws Exception {

        saveDefaultTestBatteries();

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "abc")
                        .param("endPostcode", "6003"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldPreserveLeadingZeroInPostcodeInApiResponse() throws Exception {

        BatteryRequestDto battery = createBatteryDto("BatteryWithZero", "0820", 1200);

        BatteryListRequest request = new BatteryListRequest();
        request.setBatteries(List.of(battery));

        mockMvc.perform(post("/api/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/batteries/search")
                        .param("startPostcode", "0820")
                        .param("endPostcode", "0820"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryNames[0]").value("BatteryWithZero"));
    }

} 