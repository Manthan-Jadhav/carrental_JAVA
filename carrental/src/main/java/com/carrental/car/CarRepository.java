package com.carrental.car;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CarRepository extends JpaRepository<Car, Integer> {

    List<Car> findByStatusIgnoreCase(String status);

//     @Query(value = """
//             SELECT * FROM CAR
//             WHERE LOWER(status) = LOWER(:status)
//               AND branch_id = :branchId
//             """, nativeQuery = true)
    List<Car> findAvailableByBranch(
            @Param("status") String status,
            @Param("branchId") Integer branchId);

    List<Car> findByCategory_CategoryIdAndStatusIgnoreCase(
            Integer categoryId,
            String status
    );

    @Query(value = """
        SELECT * FROM CAR
        WHERE LOWER(status) = LOWER(:status)
          AND (:branchId IS NULL OR branch_id = :branchId)
          AND (:categoryId IS NULL OR category_id = :categoryId)
        """, nativeQuery = true)
    List<Car> findAvailableCars(
            @Param("status") String status,
            @Param("branchId") Integer branchId,
            @Param("categoryId") Integer categoryId
    );

    List<Car> findByStatusIgnoreCaseOrderByPricePerDayAsc(
            String status);

    List<Car> findByStatusIgnoreCaseOrderByPricePerDayDesc(
            String status);

    @Query(value = """
    SELECT * FROM CAR
    WHERE LOWER(status) = LOWER(:status)
      AND (:branchId IS NULL OR branch_id = :branchId)
      AND (:categoryId IS NULL OR category_id = :categoryId)
    ORDER BY
      CASE WHEN :sortOrder = 'asc' THEN price_per_day END ASC,
      CASE WHEN :sortOrder = 'desc' THEN price_per_day END DESC
    """, nativeQuery = true)
    List<Car> findAvailableCarsSorted(
            @Param("status") String status,
            @Param("branchId") Integer branchId,
            @Param("categoryId") Integer categoryId,
            @Param("sortOrder") String sortOrder
    );

    @Query(value = """
    SELECT c.* FROM CAR c
    JOIN CAR_CATEGORY cc
      ON c.category_id = cc.category_id
    WHERE LOWER(c.status) = LOWER(:status)
      AND (:branchId IS NULL OR c.branch_id = :branchId)
      AND (:categoryId IS NULL OR c.category_id = :categoryId)
      AND (:seats IS NULL OR cc.seating_capacity = :seats)
    ORDER BY
      CASE WHEN :sortOrder = 'asc' THEN c.price_per_day END ASC,
      CASE WHEN :sortOrder = 'desc' THEN c.price_per_day END DESC
    """, nativeQuery = true)
    List<Car> findAvailableCarsFiltered(
            @Param("status") String status,
            @Param("branchId") Integer branchId,
            @Param("categoryId") Integer categoryId,
            @Param("seats") Integer seats,
            @Param("sortOrder") String sortOrder
    );

    @Query(value = """
    SELECT c.* FROM CAR c
    WHERE (LOWER(c.status) = LOWER(:status) OR LOWER(c.status) = 'discontinued')
      AND (:branchId IS NULL OR c.branch_id = :branchId)
      AND (:categoryId IS NULL OR c.category_id = :categoryId)
      AND (:seats IS NULL OR EXISTS (
          SELECT 1 FROM CAR_CATEGORY cc
          WHERE cc.category_id = c.category_id
            AND cc.seating_capacity = :seats
      ))
      AND (:fuelType IS NULL OR :fuelType = ''
           OR LOWER(c.fuel_type) = LOWER(:fuelType))
    ORDER BY
      CASE WHEN :sortOrder = 'asc' THEN c.price_per_day END ASC,
      CASE WHEN :sortOrder = 'desc' THEN c.price_per_day END DESC
    """, nativeQuery = true)
    List<Car> findAvailableCarsWithFuelFilter(
            @Param("status") String status,
            @Param("branchId") Integer branchId,
            @Param("categoryId") Integer categoryId,
            @Param("seats") Integer seats,
            @Param("fuelType") String fuelType,
            @Param("sortOrder") String sortOrder
    );

    @Query(value = """
        SELECT c.* FROM CAR c
        LEFT JOIN (
            SELECT car_id, COUNT(*) as booking_count
            FROM BOOKING
            GROUP BY car_id
        ) b ON c.car_id = b.car_id
        WHERE LOWER(c.status) = LOWER(:status)
        ORDER BY COALESCE(b.booking_count, 0) DESC, c.price_per_day ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Car> findBestSellers(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT c.* FROM CAR c
        WHERE LOWER(c.status) = LOWER(:status)
        ORDER BY c.price_per_day DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Car> findTopPicks(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT c.* FROM CAR c
        WHERE LOWER(c.status) = LOWER(:status)
          AND c.car_id <> :carId
        ORDER BY
          CASE WHEN :categoryId IS NOT NULL AND c.category_id = :categoryId THEN 0 ELSE 1 END,
          c.price_per_day ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Car> findSimilarCars(
            @Param("status") String status,
            @Param("carId") Integer carId,
            @Param("categoryId") Integer categoryId,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT * FROM CAR
        WHERE (:branchId IS NULL OR branch_id = :branchId)
        ORDER BY car_id DESC
        """, nativeQuery = true)
    List<Car> findAllCarsForAdmin(@Param("branchId") Integer branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.carId = :carId")
    Optional<Car> findByIdForUpdate(
            @Param("carId") Integer carId
    );

}
