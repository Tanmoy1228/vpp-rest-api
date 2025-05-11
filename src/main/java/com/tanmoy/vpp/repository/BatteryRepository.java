package com.tanmoy.vpp.repository;

import com.tanmoy.vpp.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BatteryRepository extends JpaRepository<Battery, UUID> {

    @Query("SELECT b FROM Battery b WHERE CAST(b.postcode AS int) BETWEEN :startPostcode AND :endPostcode "
            + "AND (:minCapacity IS NULL OR b.capacity >= :minCapacity) "
            + "AND (:maxCapacity IS NULL OR b.capacity <= :maxCapacity)")
    List<Battery> findInRangeWithOptionalCapacity(@Param("startPostcode") int startPostcode,
                                                  @Param("endPostcode") int endPostcode,
                                                  @Param("minCapacity") Integer minCapacity,
                                                  @Param("maxCapacity") Integer maxCapacity);


}
