package com.tanmoy.vpp.service;

import com.tanmoy.vpp.dto.response.BatterySearchResponseDto;
import com.tanmoy.vpp.exception.InvalidRangeException;
import com.tanmoy.vpp.model.Battery;
import com.tanmoy.vpp.repository.BatteryRepository;
import com.tanmoy.vpp.service.impl.BatteryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatteryServiceImplTest {

    @Mock
    private BatteryRepository batteryRepository;

    @InjectMocks
    private BatteryServiceImpl batteryService;

    @Test
    void shouldSaveAllValidBatteries() {

        List<Battery> batteries = List.of(
                Battery.of("Battery1", "6000", 1000),
                Battery.of("Battery2", "6001", 2000)
        );

        batteryService.saveAll(batteries);

        ArgumentCaptor<List<Battery>> captor = ArgumentCaptor.forClass(List.class);
        verify(batteryRepository, times(1)).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void shouldHandleTransactionSystemExceptions() {

        List<Battery> batteries = List.of(Battery.of("Battery", "6000", 1000));

        when(batteryRepository.saveAll(anyList()))
                .thenThrow(new TransactionSystemException("Transaction failed"));

        assertThrows(TransactionSystemException.class, () -> {
            batteryService.saveAll(batteries);
        });

        verify(batteryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldRollbackTransactionWhenPartialSaveFails() {

        List<Battery> batteries = List.of(
                Battery.of("Battery", "6000", 1000),
                Battery.of("Battery", "6001", 0)
        );

        when(batteryRepository.saveAll(anyList()))
                .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            batteryService.saveAll(batteries);
        });

        verify(batteryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldHandleConcurrentSavesCorrectly() throws Exception {

        int numberOfConcurrentRequests = 50;
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentRequests);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        when(batteryRepository.saveAll(anyList())).thenAnswer(
                invocation -> invocation.<List<Battery>>getArgument(0));

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            int index = i;
            executorService.submit(() -> {
                Battery battery = Battery.of("ConcurrentBattery-" + index, "600" + (index % 10), 1000 + index);
                batteryService.saveAll(List.of(battery));
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        verify(batteryRepository, times(numberOfConcurrentRequests)).saveAll(anyList());
    }

    @Test
    void shouldReturnFilteredAndSortedBatteriesInRange() {

        Battery battery1 = Battery.of("Alpha", "6000", 1000);
        Battery battery2 = Battery.of("Beta", "6001", 2000);
        Battery battery3 = Battery.of("Gamma", "6002", 3000);

        List<Battery> mockList = List.of(battery2, battery3, battery1);

        when(batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6002, null, null)).thenReturn(mockList);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                6000, 6002, null, null);

        assertThat(response.getBatteryNames()).containsExactly("Alpha", "Beta", "Gamma");
        assertThat(response.getTotalWattCapacity()).isEqualTo(6000);
        assertThat(response.getAverageWattCapacity()).isEqualTo(2000.0);
    }

    @Test
    void shouldApplyMinAndMaxCapacityFilter() {

        Battery battery1 = Battery.of("Alpha", "6100", 500);
        Battery battery2 = Battery.of("Beta", "6101", 1500);
        Battery battery3 = Battery.of("Gamma", "6102", 2500);

        List<Battery> mockList = List.of(battery2, battery3);

        when(batteryRepository.findInRangeWithOptionalCapacity(
                6100, 6102, 1000, 3000)).thenReturn(mockList);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                6100, 6102, 1000, 3000);

        assertThat(response.getBatteryNames()).containsExactly("Beta", "Gamma");
        assertThat(response.getTotalWattCapacity()).isEqualTo(4000);
        assertThat(response.getAverageWattCapacity()).isEqualTo(2000.0);
    }

    @Test
    void shouldReturnZeroStatsForNoMatches() {

        when(batteryRepository.findInRangeWithOptionalCapacity(
                7000, 7001, null, null)).thenReturn(Collections.emptyList());

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                7000, 7001, null, null);

        assertThat(response.getBatteryNames()).isEmpty();
        assertThat(response.getTotalWattCapacity()).isEqualTo(0);
        assertThat(response.getAverageWattCapacity()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleNullCapacityFilters() {

        Battery battery1 = Battery.of("Alpha", "6000", 1000);
        Battery battery2 = Battery.of("Beta", "6001", 2000);

        List<Battery> mockList = List.of(battery1, battery2);

        when(batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6001, null, null)).thenReturn(mockList);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                6000, 6001, null, null);

        assertThat(response.getBatteryNames()).containsExactly("Alpha", "Beta");
        assertThat(response.getTotalWattCapacity()).isEqualTo(3000);
        assertThat(response.getAverageWattCapacity()).isEqualTo(1500.0);
    }

    @Test
    void shouldHandleBoundaryPostcodeValues() {

        Battery battery = Battery.of("Boundary", "9999", 1000);

        List<Battery> mockList = List.of(battery);

        when(batteryRepository.findInRangeWithOptionalCapacity(
                9999, 9999, null, null)).thenReturn(mockList);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                9999, 9999, null, null);

        assertThat(response.getBatteryNames()).containsExactly("Boundary");
        assertThat(response.getTotalWattCapacity()).isEqualTo(1000);
        assertThat(response.getAverageWattCapacity()).isEqualTo(1000.0);
    }

    @Test
    void shouldHandleLargeResultSet() {

        List<Battery> largeList = IntStream.range(0, 1000)
                .mapToObj(i -> Battery.of("Battery-" + i, "6000", 1000))
                .collect(Collectors.toList());

        when(batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6000, null, null)).thenReturn(largeList);

        long startTime = System.currentTimeMillis();

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                6000, 6000, null, null);

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(1000);
        assertThat(response.getBatteryNames()).hasSize(1000);
        assertThat(response.getTotalWattCapacity()).isEqualTo(1000000);
        assertThat(response.getAverageWattCapacity()).isEqualTo(1000.0);
    }

    @Test
    void shouldHandleZeroCapacityValues() {

        Battery battery = Battery.of("Zero", "6000", 0);

        List<Battery> mockList = List.of(battery);

        when(batteryRepository.findInRangeWithOptionalCapacity(
                6000, 6000, null, null)).thenReturn(mockList);

        BatterySearchResponseDto response = batteryService.getBatteriesByPostcodeRange(
                6000, 6000, null, null);

        assertThat(response.getBatteryNames()).containsExactly("Zero");
        assertThat(response.getTotalWattCapacity()).isEqualTo(0);
        assertThat(response.getAverageWattCapacity()).isEqualTo(0.0);
    }

    @Test
    void shouldThrowInvalidRangeExceptionWhenStartGreaterThanEnd() {
        assertThrows(InvalidRangeException.class, () -> {
            batteryService.getBatteriesByPostcodeRange(7002, 7001, null, null);
        });
    }

}