package org.example.houseorder.model;

import java.math.BigDecimal;

public class HouseOrder {

    private final String orderId;
    private final String houseId;
    private final String roomType;
    private final double area;
    private final String tenantName;
    private final String contact;
    private final String checkInDate;
    private final String checkOutDate;
    private final int leaseDays;
    private final BigDecimal rent;
    private final BigDecimal deposit;
    private final String paymentMethod;
    private final String status;

    public HouseOrder(String orderId, String houseId, String roomType, double area,
                      String tenantName, String contact, String checkInDate, String checkOutDate,
                      int leaseDays, BigDecimal rent, BigDecimal deposit,
                      String paymentMethod, String status) {
        this.orderId = orderId;
        this.houseId = houseId;
        this.roomType = roomType;
        this.area = area;
        this.tenantName = tenantName;
        this.contact = contact;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.leaseDays = leaseDays;
        this.rent = rent;
        this.deposit = deposit;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getHouseId() { return houseId; }
    public String getRoomType() { return roomType; }
    public double getArea() { return area; }
    public String getTenantName() { return tenantName; }
    public String getContact() { return contact; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getLeaseDays() { return leaseDays; }
    public BigDecimal getRent() { return rent; }
    public BigDecimal getDeposit() { return deposit; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
}
