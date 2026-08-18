package com.patrones.u1;

import java.util.List;

public class OrderService {
    private final TaxCalculator taxCalculator;
    private final OrderRepository orderRepository;
    private final EmailNotifier emailNotifier;
    private final DiscountStrategy discountStrategy;

    public OrderService(
            TaxCalculator taxCalculator,
            OrderRepository orderRepository,
            EmailNotifier emailNotifier,
            DiscountStrategy discountStrategy) {
        this.taxCalculator = taxCalculator;
        this.orderRepository = orderRepository;
        this.emailNotifier = emailNotifier;
        this.discountStrategy = discountStrategy;
    }

    public void processOrder(String orderId, String email, List<Double> prices) {
        double subtotalWithTax = taxCalculator.calculateTotal(prices);
        double finalTotal = discountStrategy.apply(subtotalWithTax);
        orderRepository.save(orderId, finalTotal);
        emailNotifier.sendConfirmation(email, orderId);
    }
}
