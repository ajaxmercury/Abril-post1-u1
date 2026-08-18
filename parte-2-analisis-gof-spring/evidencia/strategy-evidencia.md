# Evidencia de Código — Patrón Strategy en Spring Framework

- **Patrón**: Strategy (Comportamiento)
- **Clase / Interfaz**: `HandlerMapping`
- **Paquete**: `org.springframework.web.servlet`
- **Módulo**: `spring-webmvc`
- **URL oficial en GitHub**: [HandlerMapping.java en spring-projects/spring-framework](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/HandlerMapping.java)

---

## Fragmento de Código Real

El siguiente extracto muestra la definición de la interfaz contractual en [HandlerMapping.java](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/HandlerMapping.java) y su consumo polimórfico en el método `getHandler` de `DispatcherServlet.java`:

```java
// Archivo: HandlerMapping.java
package org.springframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

public interface HandlerMapping {

	@Nullable
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
```

```java
// Archivo: DispatcherServlet.java (org.springframework.web.servlet)
	@Nullable
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
			for (HandlerMapping mapping : this.handlerMappings) {
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}
```

---

## Justificación Técnica

Este fragmento demuestra la aplicación arquitectónica del patrón Strategy:
1. **Definición del Contrato de Estrategia (`HandlerMapping`)**: La interfaz estandariza el algoritmo para localizar el componente ejecutor (`HandlerExecutionChain`) a partir de una solicitud HTTP entrante (`HttpServletRequest`).
2. **Familia de Algoritmos Intercambiables**: Diferentes clases concretas implementan este contrato con diversas estrategias de resolución (por ejemplo, `RequestMappingHandlerMapping` para anotaciones `@RequestMapping`/`@GetMapping`, `BeanNameUrlHandlerMapping` para URLs asociadas a nombres de beans, o `RouterFunctionMapping` para rutas funcionales).
3. **Contexto Desacoplado (`DispatcherServlet`)**: El despachador central (`DispatcherServlet`) no contiene sentencias condicionales codificadas en duro para cada tipo de mapeo; itera polimórficamente sobre una colección de estrategias inyectadas (`List<HandlerMapping>`), permitiendo extender los mecanismos de enrutamiento sin alterar el flujo principal de despacho.
