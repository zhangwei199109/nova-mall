package com.example.order.service.enums;

public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    FINISHED,
    CANCELLED,
    CLOSED_TIMEOUT,
    PARTIAL_REFUND,
    REFUNDED;

    public static boolean canPay(OrderStatus status) {
        return status == CREATED;
    }

    public static boolean canCancel(OrderStatus status) {
        return status == CREATED;
    }

    public static boolean canShip(OrderStatus status) {
        return status == PAID;
    }

    public static boolean canFinish(OrderStatus status) {
        return status == SHIPPED;
    }

    public static boolean canAutoClose(OrderStatus status) {
        return status == CREATED;
    }
}



