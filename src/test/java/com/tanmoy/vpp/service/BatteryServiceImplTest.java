package com.tanmoy.vpp.service;

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

import java.util.List;
import java.util.concurrent.*;

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
                new Battery("Battery1", "6000", 1000),
                new Battery("Battery2", "6001", 2000)
        );

        batteryService.saveAll(batteries);

        ArgumentCaptor<List<Battery>> captor = ArgumentCaptor.forClass(List.class);
        verify(batteryRepository, times(1)).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void shouldHandleTransactionSystemExceptions() {

        List<Battery> batteries = List.of(new Battery("Battery", "6000", 1000));

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
                new Battery("Battery", "6000", 1000),
                new Battery("Battery", "6001", 0)
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
                Battery battery = new Battery("ConcurrentBattery-" + index, "600" + (index % 10), 1000 + index);
                batteryService.saveAll(List.of(battery));
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        verify(batteryRepository, times(numberOfConcurrentRequests)).saveAll(anyList());
    }

}