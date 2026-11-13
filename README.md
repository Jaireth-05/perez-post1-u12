# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Proyecto integrador · Unidad 12 · Patrones de Diseño de Software


## Tecnologías

- Java 17
- Spring Boot 3.x (Spring Web, Spring Data JPA)
- H2 (base de datos en memoria)
- Maven
- ArchUnit 1.x (tests de arquitectura, scope test)
## Contexto del problema

Los empleados registran una solicitud con categoría del gasto, monto y fecha, y adjuntan documentos soporte. El sistema debe cumplir tres requisitos funcionales:

- **RF1.** Validación en varias etapas antes de aprobar (tope por categoría, documentos soporte, sin duplicados pendientes), con nuevas verificaciones previstas a futuro.
- **RF2.** Cálculo del monto aprobado con fórmula distinta por categoría (viáticos, representación, capacitación), con nuevas categorías previstas.
- **RF3.** Notificación simultánea a varios interesados (empleado, Contabilidad, auditoría) ante cada cambio de estado, con más interesados previstos.
## Diseño Propuesto

Análisis realizado **antes** de escribir código de patrones.

### RF1: Validación en varias etapas

- **Qué varía:** el conjunto de verificaciones aplicadas a una solicitud; hoy tres, y Finanzas planea sumar más (por ejemplo, límite anual acumulado).
- **Patrón:** Chain of Responsibility.
- **Justificación:** cada verificación es un eslabón independiente que puede cortar el flujo con su propio motivo de rechazo, y agregar una nueva no obliga a modificar las existentes.
### RF2: Cálculo distinto por categoría

- **Qué varía:** la fórmula del monto aprobado según la categoría.
- **Patrón:** Strategy.
- **Justificación:** cada fórmula es un algoritmo intercambiable elegido en tiempo de ejecución según la categoría, así que una categoría nueva ("bienestar") es una clase nueva y no una edición de un `switch` ni de las fórmulas existentes.
### RF3: Notificación a varios interesados

- **Qué varía:** quiénes reaccionan a un cambio de estado y cómo (el empleado recibe el resultado, Contabilidad solo las aprobaciones, auditoría todos los cambios, y a futuro un panel de métricas).
- **Patrón:** Observer (eventos de Spring: `ApplicationEventPublisher` y `@EventListener`).
- **Justificación:** el servicio publica el cambio de estado sin conocer a los suscriptores y cada uno decide si le interesa, de modo que sumar un nuevo interesado no modifica al emisor.
### Cómo se componen los patrones

Flujo de una solicitud:

1. **Chain of Responsibility** valida la solicitud.
2. Si pasa, **Strategy** calcula el monto aprobado según la categoría.
3. Se actualiza el estado y se publica el evento; los **Observers** reaccionan.
   Cada patrón resuelve un punto de variabilidad distinto y no depende de los otros.

> _Pendiente: agregar aquí un diagrama de secuencia simple (puede ser Mermaid) del flujo anterior._

## Arquitectura y estructura de paquetes

```
src/main/java/com/empresa/reembolsos/
├── ReembolsosApplication.java
├── dominio/                 # Modelo puro, sin Spring ni JPA
│   ├── SolicitudReembolso.java
│   ├── CategoriaGasto.java
│   └── EstadoSolicitud.java
├── procesamiento/           # Donde viven los patrones
│   ├── validacion/          # RF1: Chain of Responsibility
│   ├── calculo/             # RF2: Strategy
│   └── notificacion/        # RF3: Observer
├── aplicacion/              # Servicio que orquesta el flujo
├── infraestructura/         # Persistencia (JPA)
└── adaptadores/rest/        # Controlador REST
```

### Decisiones de diseño

### Decisión 1: orden de los validadores en la cadena (RF1)

**Orden elegido:** documentos soporte → monto máximo → duplicados.

**Por qué el validador de duplicados va último.** Los dos primeros validadores son comparaciones baratas en memoria (un booleano y una búsqueda en un mapa). El de duplicados recorre la lista de solicitudes pendientes del empleado, que crece con su historial. Al ubicarlo al final, una solicitud sin documentos o con monto excedido se rechaza sin recorrer esa lista (fail-fast).

**Qué se pierde.** El empleado recibe un solo motivo de rechazo por intento: si le faltan documentos y además tiene un duplicado, se entera del segundo solo después de corregir el primero. Acumular todos los errores resolvería eso, pero dejaría de ser una cadena que se detiene y bastaría una `List<Validador>`.

**Límite del ahorro.** El servicio carga la lista de pendientes antes de invocar la cadena, así que lo que se ahorra es el recorrido, no la consulta a la base de datos.

**Orden entre documentos y monto.** Cuestan lo mismo; se pone primero documentos porque es lo que el empleado corrige más rápido.

**Corrección detectada.** El borrador original comparaba solo el mes (`getMonth()`), de modo que marzo de 2025 y marzo de 2026 contaban como duplicados. Se cambió a `YearMonth`, que compara mes y año.

## Cómo ejecutar

```bash
mvn clean package
mvn spring-boot:run
```

Para correr los tests:

```bash
mvn test
```