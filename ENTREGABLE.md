# S11 - AP5: Reto individual - Construcción de un Pipeline de Calidad

## 1. Datos del estudiante

- Estudiante: Marco Infante
- Curso: Programación de Servicios Web
- Actividad: S11 - AP5
- Proyecto: `PSW_Pipeline_Base`
- Fecha: [colocar fecha de ejecución]

## 2. Preparación y ejecución del proyecto

El proyecto es una API Spring Boot construida con Maven y Java 17. Se ejecutó mediante `mvn spring-boot:run` en el puerto 8085. Se verificaron los endpoints solicitados:

| Endpoint | Método | Resultado esperado |
| --- | --- | --- |
| `http://localhost:8085/products` | GET | Lista JSON de 5 productos |
| `http://localhost:8085/login` | POST | JSON de autenticación |

Solicitud de prueba de inicio de sesión:

```json
{
  "username": "admin",
  "password": "123456"
}
```

Resultado esperado: `success: true` y token `ABC123XYZ`.

**Evidencia 1.** Insertar aquí una captura propia de la aplicación ejecutándose y de las pruebas de ambos endpoints.

## 3. Pipeline de Jenkins

Se creó un pipeline declarativo en el archivo `Jenkinsfile`. Las etapas implementadas son:

1. **Checkout:** obtiene el código fuente desde el repositorio configurado en Jenkins.
2. **Build y pruebas unitarias:** ejecuta `mvn clean verify`, publica los resultados JUnit y el reporte de cobertura JaCoCo.
3. **Análisis SonarQube:** ejecuta `mvn sonar:sonar` usando la instalación global `SonarQube` de Jenkins.
4. **Quality Gate:** espera la respuesta de SonarQube y detiene el pipeline si no se cumple el umbral de calidad.
5. **Pruebas de carga JMeter:** ejecuta el plan `jmeter/api-load-test.jmx` y publica el reporte HTML.
6. **Notificación:** envía un mensaje de éxito o fallo al canal Slack configurado.

Para ejecutarlo en Jenkins se requiere instalar/configurar: Pipeline, SonarQube Scanner, SonarQube Quality Gates, HTML Publisher, JUnit y Slack Notification Plugin; además, definir las herramientas globales `jdk17`, `Maven 3`, el servidor SonarQube con nombre `SonarQube`, JMeter en el `PATH` del agente y la integración de Slack.

**Evidencia 2.** Insertar aquí la captura propia de la vista Stage View o Blue Ocean con las etapas del pipeline.

## 4. Análisis de calidad con SonarQube

El análisis usa `sonar-project.properties` y recibe el reporte XML de JaCoCo generado en `target/site/jacoco/jacoco.xml`. Se deben registrar en la ejecución propia los indicadores de Bugs, Vulnerabilidades, Code Smells, Coverage y Duplications.

| Problema identificado en el código base | Por qué afecta al proyecto | Mejora propuesta |
| --- | --- | --- |
| Credenciales fijas (`admin` / `123456`) y token fijo en `AuthController`. | Expone secretos, permite autenticación predecible y no es apto para un entorno real. | Gestionar credenciales con Spring Security, almacenar contraseñas con hash y emitir tokens firmados con expiración. |
| El método `validateProduct` usa varios `if` anidados. | Reduce la legibilidad y eleva la complejidad cognitiva, haciendo más difícil probar o modificar la validación. | Usar guard clauses: devolver `false` temprano para producto nulo, nombre vacío o precio no positivo. |
| `Double` se utiliza para representar precios. | Los números de punto flotante pueden introducir errores de precisión en operaciones monetarias. | Reemplazar `Double` por `BigDecimal`, definir escala y redondeo explícitos. |

**Evidencia 3.** Insertar aquí una captura propia del dashboard de SonarQube, incluyendo las métricas y los hallazgos reportados por la herramienta. Si SonarQube clasifica los hallazgos con otra regla o severidad, conservar la clasificación real de la ejecución y reemplazar la descripción de esta tabla por esa evidencia.

## 5. Configuración de JMeter

El archivo `jmeter/api-load-test.jmx` contiene una prueba con **50 usuarios concurrentes**, un ramp-up de 10 segundos y una iteración por usuario. Cada usuario ejecuta:

1. `GET /products`.
2. `POST /login` con las credenciales de prueba.

El plan valida que ambas respuestas tengan código HTTP 200. Por tanto, la ejecución genera hasta 100 solicitudes en total. Desde Jenkins se ejecuta en modo no gráfico y el reporte HTML queda en `target/jmeter-report`.

Comando de ejecución manual:

```powershell
jmeter -n -t jmeter/api-load-test.jmx -l target/jmeter-results.jtl -e -o target/jmeter-report -Jhost=localhost -Jport=8085
```

| Métrica | GET /products | POST /login |
| --- | ---: | ---: |
| Solicitudes | [dato real] | [dato real] |
| Tiempo medio de respuesta (ms) | [dato real] | [dato real] |
| Throughput (solicitudes/s) | [dato real] | [dato real] |
| Porcentaje de errores | [dato real] | [dato real] |

**Evidencia 4.** Insertar aquí una captura propia de la configuración del Thread Group y una captura del reporte HTML de JMeter.

## 6. Análisis de resultados de carga

Completar esta sección exclusivamente con las métricas obtenidas en la tabla anterior:

1. El endpoint con mejor comportamiento fue **[endpoint con menor tiempo y 0 % de errores]**, pues presentó un tiempo medio de **[x] ms** y un throughput de **[x] solicitudes/s**.
2. El endpoint con mayor tiempo de respuesta fue **[endpoint]**, con **[x] ms** de promedio.
3. Se produjeron **[sí/no]** errores. El porcentaje observado fue **[x] %**. Si hubo errores, se revisaron los códigos y mensajes en el archivo `target/jmeter-results.jtl`.
4. Con la carga de 50 usuarios concurrentes, el sistema **[soportó/no soportó]** la carga porque **[explicar usando tiempo de respuesta, throughput y porcentaje de errores]**.
5. Se recomienda **[mejora basada en los datos]**. Una mejora razonable si `/login` es el punto lento sería implementar autenticación real con almacenamiento seguro y una estrategia de caché o escalamiento; si `/products` es el punto lento, usar paginación y caché de catálogo.

## 7. Integración con Slack

En la sección `post` del `Jenkinsfile` se configuraron dos notificaciones:

- `success`: mensaje verde cuando todas las etapas finalizan correctamente.
- `failure`: mensaje rojo cuando cualquier etapa falla.

El mensaje incluye el nombre del job, el número de build y el enlace a la ejecución. Antes de ejecutar se debe configurar el workspace, token y canal mediante el plugin Slack Notification de Jenkins; el canal predeterminado usado por el archivo es `#calidad`.

**Evidencia 5.** Insertar aquí una captura propia del mensaje recibido en Slack después de una ejecución exitosa y, de ser posible, otra de una ejecución fallida controlada.

## 8. Conclusiones

1. El pipeline automatiza la verificación de compilación, pruebas, cobertura, análisis estático, prueba de carga y comunicación del resultado, reduciendo la detección tardía de problemas.
2. JaCoCo y SonarQube permiten observar la cobertura y la mantenibilidad antes de integrar cambios. Los problemas de seguridad y de diseño encontrados en el código base deben corregirse antes de un despliegue productivo.
3. JMeter permite validar el comportamiento de los endpoints bajo concurrencia; la decisión de que el sistema soporta la carga debe basarse en las métricas reales de tiempo de respuesta, throughput y errores.
4. La notificación en Slack hace visible el resultado del pipeline para el equipo y facilita actuar de inmediato ante una falla.
