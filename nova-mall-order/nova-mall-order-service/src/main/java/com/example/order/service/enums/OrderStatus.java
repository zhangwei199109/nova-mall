package com.example.order.service.enums;

public enum OrderStatus {
    CREATED,
    PAID,
    CANCELLED;

    public static boolean canPay(OrderStatus status) {
        return status == CREATED;
    }

    public static boolean canCancel(OrderStatus status) {
        return status == CREATED;
    }
}



