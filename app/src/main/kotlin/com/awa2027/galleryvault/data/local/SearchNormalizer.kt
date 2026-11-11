package com.awa2027.galleryvault.data.local

/**
 * Clave de búsqueda que se guarda en la columna search_text (la usa el mapper del taller).
 *
 * Reto, parte 2: hoy devuelve el texto tal cual. Lee el contrato de búsqueda en la guía del reto del curso:
 * SQLite LOWER() y LIKE solo entienden ASCII, así que "velazquez" no encuentra "Velázquez".
 */
fun normalizeForSearch(text: String): String = text
