# Evidencia de Código — Patrón Singleton en Spring Framework

- **Patrón**: Singleton (Creacional)
- **Clase**: `DefaultSingletonBeanRegistry`
- **Paquete**: `org.springframework.beans.factory.support`
- **Módulo**: `spring-beans`
- **URL oficial en GitHub**: [DefaultSingletonBeanRegistry.java en spring-projects/spring-framework](https://github.com/spring-projects/spring-framework/blob/main/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java)

---

## Fragmento de Código Real

El siguiente extracto proviene de la definición de estructuras internas y el método de acceso concurrente en [DefaultSingletonBeanRegistry.java](https://github.com/spring-projects/spring-framework/blob/main/spring-beans/src/main/java/org/springframework/beans/factory/support/DefaultSingletonBeanRegistry.java):

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

---

## Justificación Técnica

Este fragmento demuestra la implementación del patrón Singleton gestionado por contenedor (Registry of Singletons):
1. **Almacenamiento Centralizado (`singletonObjects`)**: El mapa concurrente `singletonObjects` actúa como la tabla central de instancias compartidas, asegurando que para un identificador de bean (`beanName`) exista a lo sumo una única instancia viva en el contexto.
2. **Ciclo de Vida y Resolución Concurrente**: Mediante `earlySingletonObjects`, `singletonFactories` y el cerrojo de sincronización `singletonLock`, la clase implementa un mecanismo de caché multinivel capaz de resolver dependencias circulares y garantizar inicialización segura entre hilos concurrentes.
3. **Punto Único de Acceso Controlado (`getSingleton`)**: El método `getSingleton(String)` expone la interfaz estandarizada `SingletonBeanRegistry` para consultar y reutilizar instancias sin instanciación redundante.
