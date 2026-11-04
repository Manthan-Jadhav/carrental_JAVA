
package com.carrental.car;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKING")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "car_id")
    private Integer carId;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "rental_mode")
    private String rentalMode;

    @Column(name = "km_package")
    private String kmPackage;

    @Column(name = "daily_rate")
    private BigDecimal dailyRate;

    @Column(name = "rental_amount")
    private BigDecimal rentalAmount;

    @Column(name = "km_charge")
    private BigDecimal kmCharge;

    @Column(name = "coverage_charge")
    private BigDecimal coverageCharge;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Booking() {
    }

    // Generate getters and setters in IntelliJ


    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getRentalMode() {
        return rentalMode;
    }

    public void setRentalMode(String rentalMode) {
        this.rentalMode = rentalMode;
    }

    public String getKmPackage() {
        return kmPackage;
    }

    public void setKmPackage(String kmPackage) {
        this.kmPackage = kmPackage;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public BigDecimal getRentalAmount() {
        return rentalAmount;
    }

    public void setRentalAmount(BigDecimal rentalAmount) {
        this.rentalAmount = rentalAmount;
    }

    public BigDecimal getKmCharge() {
        return kmCharge;
    }

    public void setKmCharge(BigDecimal kmCharge) {
        this.kmCharge = kmCharge;
    }

    public BigDecimal getCoverageCharge() {
        return coverageCharge;
    }

    public void setCoverageCharge(BigDecimal coverageCharge) {
        this.coverageCharge = coverageCharge;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "car_id",
            insertable = false,
            updatable = false
    )
    private Car car;

    public Car getCar() {
        return car;
    }

}
