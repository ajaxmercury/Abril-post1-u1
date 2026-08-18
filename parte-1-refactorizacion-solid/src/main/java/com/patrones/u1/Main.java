package com.patrones.u1;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaxCalculator taxCalculator = new TaxCalculator(0.19);
        OrderRepository orderRepository = new OrderRepository();
        EmailNotifier emailNotifier = new EmailNotifier();
        OrderReporter orderReporter = new OrderReporter();

        OrderService vipOrderService = new OrderService(
                taxCalculator,
                orderRepository,
                emailNotifier,
                new VipDiscount()
        );
        vipOrderService.processOrder("ORD-001", "vip@mail.com", List.of(100.0, 200.0, 50.0));

        OrderService regularOrderService = new OrderService(
                taxCalculator,
                orderRepository,
                emailNotifier,
                new RegularDiscount()
        );
        regularOrderService.processOrder("ORD-002", "reg@mail.com", List.of(80.0, 120.0));

        orderReporter.print(orderRepository.findAll());
    }
}
