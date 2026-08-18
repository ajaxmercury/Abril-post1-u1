# Post-contenido — Refactorización SOLID y análisis de patrones GoF en Spring

Este repositorio contiene el desarrollo académico integral del taller sobre principios de diseño orientado a objetos y patrones de software (Unidad 1). La actividad se divide en dos fases complementarias:

1. **Parte 1 — Refactorización SOLID**: Descomposición y rediseño de una clase monolítica (*God Object*) hacia una arquitectura modular, extensible y de bajo acoplamiento aplicando principios SOLID y el patrón Strategy.
2. **Parte 2 — Patrones GoF en Spring Framework**: Investigación técnica basada en código fuente real y verificable del repositorio oficial [spring-projects/spring-framework](https://github.com/spring-projects/spring-framework) para tres patrones GoF de diferentes categorías.

---

## Parte 1 — Refactorización SOLID

### El God Object Original (`OrderProcessor`)
El punto de partida del ejercicio corresponde a la clase [OrderProcessor.java](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderProcessor.java), la cual concentraba en una sola entidad el cálculo de impuestos, las políticas de descuento, el almacenamiento en memoria, el envío de notificaciones por correo y el formateo de reportes en consola.

A continuación se resumen las violaciones identificadas:

| Principio | Parte afectada | Violación |
|---|---|---|
| SRP | OrderProcessor | La clase tiene múltiples responsabilidades no cohesivas (impuestos, descuentos, persistencia, notificación y reporte). |
| OCP | applyDiscount | Agregar tipos de descuento requiere modificar código existente mediante estructuras condicionales rígidas. |
| DIP | Dependencias de OrderProcessor | La clase depende directamente de detalles concretos y recursos de bajo nivel sin utilizar abstracciones ni inyección. |

### Solución a Cada Violación
- **Solución a SRP**: Se dividió la funcionalidad en clases cohesivas con una única razón para cambiar:
  - `TaxCalculator`: Encapsula el cálculo de impuestos y subtotales.
  - `OrderRepository`: Gestiona el almacenamiento de órdenes en memoria y retorna colecciones inmutables.
  - `EmailNotifier`: Se encarga del envío de confirmaciones.
  - `OrderReporter`: Formatea e imprime las órdenes en consola.
- **Solución a OCP**: Se definió la interfaz `DiscountStrategy` y se implementaron clases independientes (`VipDiscount`, `RegularDiscount`, `NoDiscount`), permitiendo agregar nuevas políticas de descuento sin tocar el código existente.
- **Solución a DIP**: Se creó `OrderService`, el cual recibe todas sus dependencias mediante inyección por constructor (`TaxCalculator`, `OrderRepository`, `EmailNotifier`, `DiscountStrategy`), dependiendo exclusivamente de abstracciones y sin instanciar objetos con `new` en su interior.

---

## Estructura de la solución

Dentro de `parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/` se implementaron los siguientes componentes (ninguno supera las 40 líneas de código):

- **[TaxCalculator](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/TaxCalculator.java)**: Recibe la tasa de impuesto por constructor y calcula el total con impuestos a partir de una lista de precios.
- **[OrderRepository](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderRepository.java)**: Almacena órdenes y expone `findAll()` mediante una colección protegida (`Collections.unmodifiableList`).
- **[EmailNotifier](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/EmailNotifier.java)**: Envía confirmaciones de órdenes registradas.
- **[OrderReporter](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderReporter.java)**: Imprime en consola el reporte formateado de órdenes.
- **[DiscountStrategy](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/DiscountStrategy.java)**: Interfaz contractual para la familia de algoritmos de descuento.
- **[VipDiscount](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/VipDiscount.java)**: Implementación del 15% de descuento (`total * 0.85`).
- **[RegularDiscount](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/RegularDiscount.java)**: Implementación del 5% de descuento (`total * 0.95`).
- **[NoDiscount](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/NoDiscount.java)**: Implementación sin descuento (`total`).
- **[OrderService](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/OrderService.java)**: Orquestador del flujo de procesamiento que recibe sus dependencias por constructor.
- **[Main](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-1-refactorizacion-solid/src/main/java/com/patrones/u1/Main.java)**: Clase ejecutable que demuestra el flujo completo con clientes VIP y REGULAR.

---

## Cómo ejecutar Parte 1

Para compilar y ejecutar el proyecto desde la terminal:

```bash
cd parte-1-refactorizacion-solid
mvn compile
mvn exec:java -Dexec.mainClass="com.patrones.u1.Main"
```

---

## Parte 2 — Patrones GoF en Spring

En el directorio [parte-2-analisis-gof-spring/](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-2-analisis-gof-spring) se encuentra el documento de análisis académico ([documento-analisis.md](file:///c:/Users/david/OneDrive/Documents/Patrones%20de%20diseño/parte-2-analisis-gof-spring/documento-analisis.md)) y los archivos individuales de evidencia técnica dentro de `evidencia/`:

| Patrón | Categoría | Implementación analizada |
|---|---|---|
| Singleton | Creacional | DefaultSingletonBeanRegistry |
| Proxy | Estructural | JdkDynamicAopProxy |
| Strategy | Comportamiento | HandlerMapping |

---

## Herramientas

- Java 17 (Eclipse Adoptium OpenJDK)
- Maven
- Git
- GitHub
- Spring Framework (código fuente oficial v6.2+)

---

## Conclusiones

La articulación entre los principios SOLID y los patrones de diseño GoF constituye la base fundamental de la arquitectura de software mantenible, extensible y testeable. Mediante la Inversión de Dependencias (DIP) y la segregación de responsabilidades (SRP), el software desacopla su lógica de negocio de la infraestructura y de los algoritmos variables (como se demostró con el patrón Strategy y la inyección por constructor). El análisis de Spring Framework confirma que estos mismos principios rigen a escala industrial en componentes centrales como `DefaultSingletonBeanRegistry` (gestión de ciclo de vida singleton), `JdkDynamicAopProxy` (intercepción transparente de aspectos) y `HandlerMapping` (enrutamiento polimórfico de solicitudes HTTP), consolidando una arquitectura limpia y preparada para evolucionar.
