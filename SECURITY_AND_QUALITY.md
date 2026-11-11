# SECURITY_AND_QUALITY.md (Unidad 2)

Marca cada casilla solo si lo verificaste en tu código, y escribe al lado **dónde** (archivo y función o test). Una casilla marcada sin evidencia no cuenta.

## 1. Datos locales

- [ ] Ninguna sincronización borra ni reinicia `is_favorite` ni `favorited_at`. Evidencia:
- [ ] Ninguna consulta SQL concatena texto del usuario; todas usan parámetros de `@Query`. Evidencia:
- [ ] Los comodines `%` y `_` del texto buscado se tratan como literales. Evidencia:
- [ ] La base de Room se excluye de los respaldos en la nube. Evidencia:

## 2. Red y errores

- [ ] La app solo usa HTTPS (sin tráfico en texto plano). Evidencia:
- [ ] Toda excepción de red, de JSON o de Room se convierte en un `AppError` y la UI muestra un mensaje en español sin detalles técnicos. Evidencia:
- [ ] `CancellationException` no se convierte en error. Evidencia:
- [ ] Un fallo de red con datos guardados nunca muestra un error a pantalla completa. Evidencia:

## 3. Tests y calidad

- [ ] Ningún test usa la red real ni un emulador. Evidencia:
- [ ] Mis tests propios fallarían si el mapper olvidara una columna local o si la búsqueda no escapara los comodines. Evidencia:
- [ ] `./gradlew check` (tests, ktlint y Android lint) en verde. Evidencia:

## 4. Riesgos conocidos (no se resuelven en esta unidad)

Describe en 2 o 3 líneas qué pasaría si la API devolviera datos manipulados o un JSON enorme, y qué cambiaría cuando la Unidad 3 sincronice datos propios de la persona usuaria con la nube.

> Tu respuesta:
