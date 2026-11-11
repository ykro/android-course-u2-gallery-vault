# ARCHITECTURE_DECISIONS.md (Unidad 2)

Responde con tus palabras y con referencias concretas a tu código (archivo y función). Respuestas genéricas o copiadas de la documentación no suman puntos. Extensión sugerida: 4 a 8 líneas por pregunta.

Puntaje (15 puntos, revisión manual): preguntas 1 a 5, 10 puntos; tu regla en la sección 6 de `AGENTS.md` (pregunta 6), 2; declaración de uso de IA (pregunta 7), 1; y el checklist `SECURITY_AND_QUALITY.md`, 2.

## 1. Fuente única de verdad

¿Qué observa tu `VaultViewModel` y qué devuelve `refresh()`? Describe, paso a paso, qué pasa desde que la persona hace pull-to-refresh hasta que aparece una obra nueva en la grilla, nombrando las funciones de tu código. ¿Por qué el ViewModel no recibe la lista directamente de Retrofit?

> Tu respuesta:

## 2. Resolución de conflictos red frente a local

Cuando la API y Room tienen versiones distintas de la misma obra, ¿qué columnas gana la red y cuáles gana el dispositivo? Explica por qué `@Insert(onConflict = OnConflictStrategy.REPLACE)` borraría los favoritos, cómo lo evita tu `upsertFromRemote` y qué pasa con `favorited_at`. ¿Qué harías con una obra que ya no existe en la API pero sigue marcada como favorita?

> Tu respuesta:

## 3. Búsqueda local

¿Por qué no basta `LOWER(title) LIKE '%' || :q || '%'` en SQLite para que `"velazquez"` encuentre `Velázquez`? Explica tu `normalizeForSearch`, dónde se guarda el resultado y cómo tratas `%` y `_`. ¿Qué tendrías que hacer con las filas ya guardadas si cambias la normalización en una versión futura de la app?

> Tu respuesta:

## 4. Error con caché frente a sin caché

Describe las dos pantallas que ve la persona cuando falla un refresh (con datos guardados y sin ellos) y por qué decides con `hasCache()` y no con la lista filtrada en pantalla. ¿Qué evita tu guarda de "un solo refresh a la vez"?

> Tu respuesta:

## 5. Tus tests propios

Para cada uno de tus tests en `student/`, describe un bug concreto en `app/src/main` que lo haría fallar (archivo, función y qué cambiaría). Un test que no fallaría con ningún bug plausible no suma.

> Tu respuesta:

## 6. Tu regla en AGENTS.md

Copia la regla que agregaste en la sección 6 de `AGENTS.md`. Explica qué error de un asistente de IA previene y describe una ocasión concreta en que la regla cambió (o habría cambiado) lo que el asistente te propuso.

> Tu respuesta:

## 7. Declaración de uso de IA

Marca y completa lo que aplique. Usar IA está permitido; ocultarlo no.

- [ ] No usé asistentes de IA en esta unidad.
- [ ] Usé asistentes de IA. Herramientas: ____________________
- [ ] Partes donde la usé (código, tests, documentación): ____________________
- [ ] Una sugerencia de la IA que rechacé o corregí y por qué: ____________________
