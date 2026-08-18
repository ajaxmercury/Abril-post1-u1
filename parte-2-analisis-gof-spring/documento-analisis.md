# Portada

- **Nombre**: David Alejandro Abril Pérez
- **Curso**: Patrones de Diseño de Software
- **Unidad**: Unidad 1
- **Actividad**: Post-contenido — Refactorización SOLID y análisis de patrones GoF en Spring
- **Fecha**: 18 de agosto de 2026

---

# Introducción

El diseño de software empresarial moderno se fundamenta en principios arquitectónicos y patrones de diseño orientados a mitigar la complejidad, fomentar la reutilización y garantizar la mantenibilidad del código a largo plazo. El catálogo seminal formulado por el grupo conocido como la "Banda de los Cuatro" (Gamma et al., 1994) definió una taxonomía de patrones clasificados en tres categorías fundamentales: creacionales, estructurales y de comportamiento. En conjunto con los principios SOLID formalizados por Martin (2018), estos patrones proporcionan lineamientos esenciales para construir sistemas con alta cohesión y bajo acoplamiento.

Spring Framework constituye uno de los ejemplos más destacados de la aplicación práctica de estos conceptos a gran escala en el ecosistema Java (Walls, 2022). Lejos de emplear los patrones GoF como conceptos puramente teóricos, Spring los integra como componentes centrales de su infraestructura de Inversión de Control (IoC), Programación Orientada a Aspectos (AOP) y arquitectura web MVC (Spring Framework Authors, 2026). En este documento se presenta un análisis técnico y riguroso de tres patrones GoF pertenecientes a distintas categorías presentes en el código fuente oficial de Spring Framework: el patrón Creacional *Singleton* (`DefaultSingletonBeanRegistry`), el patrón Estructural *Proxy* (`JdkDynamicAopProxy`) y el patrón de Comportamiento *Strategy* (`HandlerMapping`).

---

# Análisis del Patrón 1 — Singleton

## Definición
El patrón Singleton clásico formulado por Gamma et al. (1994) tiene como propósito garantizar que una clase disponga de una única instancia y proveer un punto de acceso global a dicha instancia mediante un método estático (`getInstance()`). En dicho esquema clásico, la restricción de unicidad se encuentra ligada estrictamente al nivel del cargador de clases (*ClassLoader*) de la máquina virtual Java.

En contraste, el enfoque adoptado por Spring Framework difiere sustancialmente del Singleton clásico:
- En Spring, el alcance (*scope*) Singleton no se define a nivel de máquina virtual ni depende de constructores privados u operaciones estáticas.
- La unicidad se gestiona y delimita a nivel del contenedor de Inversión de Control (`ApplicationContext` o `BeanFactory`) mediante un registro centralizado de singletons (*Registry of Singletons*).
- De este modo, una clase gestionada por Spring es un objeto plano estándar (POJO) que puede ser instanciado libremente en pruebas unitarias sin acoplamiento estático, siendo el contenedor el encargado de garantizar que dentro de un contexto particular exista únicamente una instancia compartida por identificador de bean (Walls, 2022).

## Categoría
Creacional.

## Ubicación en Spring
`org.springframework.beans.factory.support.DefaultSingletonBeanRegistry`

## Módulo
`spring-beans`

## Problema que resuelve
En aplicaciones empresariales, la instanciación indiscriminada de componentes pesados (como capas de servicio, repositorios de datos o gestores transaccionales) generaría un consumo excesivo de memoria, degradación del rendimiento por recolección de basura y problemas de sincronización al carecer de un estado compartido coherente. Asimismo, la inicialización concurrente de beans en contextos multihilo y la presencia de dependencias circulares entre componentes requieren un mecanismo seguro y controlado de almacenamiento, instanciación temprana y destrucción coordinada.

## Cómo funciona
Spring centraliza la administración de beans singleton en la clase `DefaultSingletonBeanRegistry`, la cual implementa la interfaz contractual `SingletonBeanRegistry`. Esta clase gestiona un sistema de caché concurrente de múltiples niveles:
1. `singletonObjects`: Almacena los beans singleton completamente inicializados listos para su uso.
2. `earlySingletonObjects`: Almacena referencias tempranas para resolver referencias circulares antes de completar la inyección de dependencias.
3. `singletonFactories`: Registra fábricas (`ObjectFactory`) para la instanciación diferida de objetos expuestos tempranamente.
4. `singletonLock`: Coordina la sincronización de hilos concurrentes para evitar condiciones de carrera durante la instanciación.

## Evidencia del código fuente
A continuación se presenta el fragmento real obtenido del repositorio oficial de Spring Framework ([DefaultSingletonBeanRegistry.java](https://github.com/spring-projects/spring-framework/blob/main/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java)):

```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	/** Common lock for singleton creation. */
	final Lock singletonLock = new ReentrantLock();

	/** Cache of singleton objects: bean name to bean instance. */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** Cache of early singleton objects: bean name to bean instance. */
	private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

	/** Creation-time registry of singleton factories: bean name to ObjectFactory. */
	private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);

	@Override
	@Nullable
	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}
```

El código evidencia cómo `singletonObjects` actúa como la tabla centralizada de instancias vivas asociadas a su nombre identificador (`beanName`). Cuando un componente invoca `getSingleton(beanName)`, el registro consulta primero la caché concurrente sin incurrir en bloqueos globales; si se requiere resolución de ciclos o instanciación, `getSingleton(String, boolean)` coordina la consulta de `earlySingletonObjects` y `singletonFactories` bajo la protección de `singletonLock`, garantizando consistencia atómica y aislamiento entre hilos (Spring Framework Authors, 2026).

## Relación con SOLID
- **Principio de Responsabilidad Única (SRP)**: `DefaultSingletonBeanRegistry` asume la responsabilidad exclusiva de gestionar el ciclo de vida, almacenamiento y recuperación de beans singleton en caché. Delega la creación detallada por reflexión y el análisis de metadatos a otras clases especializadas como `AbstractAutowireCapableBeanFactory`.
- **Principio de Inversión de Dependencias (DIP)**: Los clientes y servicios de negocio dependen de abstracciones inyectadas por el contenedor y no invocan métodos estáticos ni conocen la implementación interna del registro de singletons (Martin, 2018).

## Análisis contrafactual
Si el contenedor de Spring no gestionara centralmente estas instancias a través de `DefaultSingletonBeanRegistry`:
1. Cada desarrollador se vería forzado a implementar el patrón Singleton clásico con métodos estáticos en cada clase, impidiendo la sustitución por dobles de prueba (*mocks*) en pruebas unitarias.
2. Sería inviable resolver dependencias circulares de manera no destructiva, ocasionando errores de desbordamiento de pila (*StackOverflowError*).
3. No se dispondría de un control centralizado del orden de cierre y liberación de recursos (`DisposableBean`), produciendo fugas de memoria y bloqueos en descriptores de red y bases de datos.

---

# Análisis del Patrón 2 — Proxy

## Definición
De acuerdo con Gamma et al. (1994), el patrón Proxy proporciona un objeto intermediario o sustituto para controlar el acceso a otro objeto. En Spring AOP, los proxies se emplean para intercalar comportamientos transversales (*cross-cutting concerns*) —como demarcación transaccional (`@Transactional`), control de seguridad (`@PreAuthorize`) y auditoría— de forma totalmente transparente tanto para el cliente como para el objeto de negocio destino (*target*) (Walls, 2022).

En Spring AOP coexisten dos mecanismos fundamentales:
1. **JDK Dynamic Proxies**: Basados en `java.lang.reflect.Proxy`. Requieren que la clase destino implemente interfaces de negocio. El proxy generado implementa dinámicamente dichas interfaces e intercepta las operaciones mediante `InvocationHandler`.
2. **CGLIB / Byte-Buddy Proxies**: Generan dinámicamente subclases de la clase destino en tiempo de ejecución manipulando *bytecode*, utilizados cuando el bean no implementa interfaces o cuando se fuerza explícitamente el uso de clases concretas.

## Categoría
Estructural.

## Ubicación en Spring
`org.springframework.aop.framework.JdkDynamicAopProxy`

## Módulo
`spring-aop`

## Problema que resuelve
En sistemas empresariales, las preocupaciones transversales suelen dispersarse a lo largo de múltiples capas de la aplicación. Si la gestión de transacciones, la verificación de permisos y la captura de trazas se incluyeran manualmente en cada método de negocio, se violaría la cohesión de las clases y se multiplicaría el código duplicado (*boilerplate*).

## Cómo funciona
Spring AOP delega la intercepción en clases como `JdkDynamicAopProxy`. Cuando un cliente invoca una operación sobre un bean proxificado, el flujo es derivado hacia el método `invoke(Object proxy, Method method, Object[] args)`. Este método consulta la cadena de interceptores (*advices*) aplicables a dicho método (`getInterceptorsAndDynamicInterceptionAdvice`), construye un objeto `ReflectiveMethodInvocation` y ejecuta la cadena mediante `invocation.proceed()`. Una vez ejecutados los aspectos previos (*before/around*), se delega la invocación real por reflexión sobre el objeto destino (`target`) y se procesan los aspectos posteriores (*after*) (Spring Framework Authors, 2026).

## Evidencia del código fuente
A continuación se presenta el fragmento real obtenido de [JdkDynamicAopProxy.java](https://github.com/spring-projects/spring-framework/blob/main/spring-aop/src/main/java/org/springframework/aop/framework/JdkDynamicAopProxy.java):

```java
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
		}
		return Proxy.newProxyInstance(determineClassLoader(classLoader), this.cache.proxiedInterfaces, this);
	}

	@Override
	@Nullable
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// ...
		List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
		if (chain.isEmpty()) {
			Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
			retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
		}
		else {
			MethodInvocation invocation =
					new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
			retVal = invocation.proceed();
		}
		return retVal;
	}
```

El método `getProxy` evidencia la creación del proxy dinámico delegando en `Proxy.newProxyInstance` con las interfaces proxificadas (`cache.proxiedInterfaces`) y la referencia `this` como `InvocationHandler`. En el método `invoke`, el proxy recupera la cadena de interceptores y ejecuta la invocación mediante `ReflectiveMethodInvocation`, permitiendo la ejecución de aspectos antes y después de transferir el control al objeto `target`.

## Relación con SOLID
- **Principio de Responsabilidad Única (SRP)**: Las clases de servicio mantienen únicamente la responsabilidad de ejecutar la lógica de dominio; las responsabilidades de infraestructura (transacciones, seguridad, logging) quedan encapsuladas en interceptores independientes (Martin, 2018).
- **Principio de Abierto/Cerrado (OCP)**: Es posible agregar nuevos comportamientos y aspectos a clases existentes sin modificar su código fuente.

## Análisis contrafactual
Si Spring no proporcionara la abstracción del patrón Proxy en AOP:
1. Cada método de servicio requeriría bloques explícitos de código `try-catch-finally` con llamadas directas a gestores transaccionales y de seguridad.
2. Cualquier modificación en la política de transacciones o seguridad implicaría alterar cientos de clases de negocio de forma manual.
3. La probabilidad de errores humanos (como omitir el cierre de una transacción) aumentaría significativamente.

---

# Análisis del Patrón 3 — Strategy

## Definición
El patrón Strategy, según Gamma et al. (1994), define una familia de algoritmos, encapsula cada uno de ellos y los hace intercambiables, permitiendo que el algoritmo varíe de forma independiente de los clientes que lo utilizan.

En Spring MVC, el componente despachador central `DispatcherServlet` debe resolver qué controlador o manejador es responsable de atender una solicitud HTTP entrante (`HttpServletRequest`). Aunque Spring no catalogue formalmente la interfaz con el sufijo "Strategy" en su documentación comercial, la arquitectura de `HandlerMapping` puede analizarse rigurosamente como una aplicación de la estructura del patrón Strategy porque:
- `HandlerMapping` define la interfaz contractual abstracta de la estrategia (`getHandler`).
- Diversas clases concretas implementan algoritmos radicalmente distintos de mapeo (`RequestMappingHandlerMapping` basado en anotaciones, `BeanNameUrlHandlerMapping` basado en nombres de beans, `RouterFunctionMapping` para rutas funcionales).
- `DispatcherServlet` actúa como el Contexto del patrón, delegando la resolución de la solicitud de forma polimórfica sin conocer los detalles de cada algoritmo (Walls, 2022).

## Categoría
Comportamiento.

## Ubicación en Spring
`org.springframework.web.servlet.HandlerMapping` (consumida por `org.springframework.web.servlet.DispatcherServlet`)

## Módulo
`spring-webmvc`

## Problema que resuelve
En un servidor web modular, las solicitudes HTTP pueden requerir diferentes mecanismos de enrutamiento (por convenciones de URL, metadatos de anotaciones o programación funcional). Si el despachador principal tuviera acopladas todas estas estrategias mediante condicionales `if-else` o `switch`, la incorporación de un nuevo mecanismo de enrutamiento exigiría alterar el núcleo de despacho de Spring MVC.

## Cómo funciona
Spring MVC define la abstracción `HandlerMapping` con la firma `HandlerExecutionChain getHandler(HttpServletRequest request)`. `DispatcherServlet` recibe una lista polimórfica de estrategias inyectadas (`List<HandlerMapping>`). Cuando arriba una petición HTTP, el despachador itera sobre cada estrategia en orden de prioridad hasta que una retorne una cadena de ejecución válida (`HandlerExecutionChain`) con el controlador y los interceptores correspondientes (Spring Framework Authors, 2026).

## Evidencia del código fuente
A continuación se contrasta el código del contrato de estrategia en [HandlerMapping.java](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/HandlerMapping.java) con su ejecución en [DispatcherServlet.java](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/DispatcherServlet.java):

```java
// Archivo: HandlerMapping.java (Estrategia Abstracta)
package org.springframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

public interface HandlerMapping {

	@Nullable
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
```

```java
// Archivo: DispatcherServlet.java (Contexto que ejecuta la Estrategia)
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

El método `getHandler` de `DispatcherServlet` demuestra el desacoplamiento propio del patrón Strategy: no inspecciona el tipo concreto de mapeo, sino que invoca polimórficamente `mapping.getHandler(request)`. La primera estrategia que reconoce la petición retorna el manejador adecuado.

## Relación con SOLID
- **Principio de Abierto/Cerrado (OCP)**: Spring MVC está abierto a la extensión (se pueden incorporar nuevas estrategias de mapeo de solicitudes simplemente implementando `HandlerMapping`) y cerrado a la modificación (no se requiere modificar `DispatcherServlet`) (Martin, 2018).
- **Principio de Inversión de Dependencias (DIP)**: `DispatcherServlet` depende exclusivamente de la abstracción contractual `HandlerMapping` y no de implementaciones concretas de enrutamiento.

## Análisis contrafactual
Si `DispatcherServlet` no utilizara la abstracción del patrón Strategy:
1. Operaría como un *God Object* monolítico lleno de sentencias condicionales acopladas a cada tipo existente de controlador.
2. Sería imposible que librerías externas o desarrolladores agregaran nuevos esquemas de enrutamiento sin reescribir el despachador.
3. Se violarían simultáneamente OCP y DIP al depender directamente de detalles de implementación de bajo nivel.

---

# Conclusiones

El desarrollo integral de este taller permitió constatar la estrecha convergencia entre los principios SOLID y los patrones de diseño GoF como fundamentos esenciales para la arquitectura de software mantenible y extensible. En la Parte 1, la refactorización de una clase *God Object* (`OrderProcessor`) demostró cómo la segregación de responsabilidades (SRP), el desacoplamiento de algoritmos de descuento mediante el patrón Strategy (OCP) y la Inyección de Dependencias por constructor (DIP) transforman un diseño frágil en una arquitectura modular y fácilmente verificable. En la Parte 2, el análisis sobre el código fuente de Spring Framework confirmó que estos mismos principios rigen los componentes nucleares de los marcos de trabajo empresariales a gran escala: la gestión de ciclo de vida en `DefaultSingletonBeanRegistry` (Singleton), la aplicación no invasiva de aspectos transversales en `JdkDynamicAopProxy` (Proxy) y el enrutamiento polimórfico y desacoplado de peticiones web en `HandlerMapping` (Strategy). En conclusión, la adopción rigurosa de estos patrones e inversión de dependencias constituye una competencia técnica indispensable para diseñar soluciones de software robustas y preparadas para su evolución.

---

# Referencias

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
- Martin, R. C. (2018). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
- Spring Framework Authors. (2026). *Spring Framework Source Code Repository (v6.2+)*. GitHub. https://github.com/spring-projects/spring-framework
- Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.
