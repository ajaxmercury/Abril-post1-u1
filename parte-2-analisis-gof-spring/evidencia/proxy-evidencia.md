# Evidencia de Código — Patrón Proxy en Spring Framework

- **Patrón**: Proxy (Estructural)
- **Clase**: `JdkDynamicAopProxy`
- **Paquete**: `org.springframework.aop.framework`
- **Módulo**: `spring-aop`
- **URL oficial en GitHub**: [JdkDynamicAopProxy.java en spring-projects/spring-framework](https://github.com/spring-projects/spring-framework/blob/main/spring-aop/src/main/java/org/springframework/aop/framework/JdkDynamicAopProxy.java)

---

## Fragmento de Código Real

El siguiente fragmento extraído de [JdkDynamicAopProxy.java](https://github.com/spring-projects/spring-framework/blob/main/spring-aop/src/main/java/org/springframework/aop/framework/JdkDynamicAopProxy.java) ilustra la generación del proxy dinámico de Java (`getProxy`) y la intercepción de invocaciones para ejecutar la cadena de interceptores AOP (`invoke`):

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

---

## Justificación Técnica

Este fragmento demuestra la realización del patrón Proxy estructural:
1. **Sustitución Transparente de Interfaz (`Proxy.newProxyInstance`)**: La clase implementa `InvocationHandler` y genera un objeto sustituto que expone las mismas interfaces que el objeto destino (`proxiedInterfaces`). El cliente interactúa con el proxy creyendo comunicarse directamente con el servicio real.
2. **Intercepción y Control de Acceso (`invoke`)**: Cuando se ejecuta cualquier método sobre el proxy, el flujo es desviado hacia `invoke(...)`. Allí se obtiene la cadena de *advices* o interceptores (transacciones, seguridad, logging) y se envuelve la invocación en `ReflectiveMethodInvocation`.
3. **Delegación al Objeto Real (`target`)**: El proxy no altera la lógica de negocio central; coordina los aspectos transversales y delega la ejecución final al objeto destino (`target`), cumpliendo la definición del patrón GoF.
