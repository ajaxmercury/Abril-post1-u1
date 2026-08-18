package com.patrones.u1;

import java.util.List;

public class OrderReporter {

    public void print(List<String> orders) {
        System.out.println("=== Reporte de Órdenes ===");
        for (String o : orders) {
            System.out.println("  " + o);
        }
    }
}
