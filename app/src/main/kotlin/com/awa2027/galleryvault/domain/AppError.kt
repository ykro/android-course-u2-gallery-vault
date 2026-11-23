package com.awa2027.galleryvault.domain

/**
 * Errores tipados de la app. La UI decide qué mostrar con un `when` exhaustivo,
 * sin leer mensajes de excepciones ni stack traces.
 */
sealed interface AppError {
    /** Sin conexión, DNS caído o tiempo de espera agotado (IOException). */
    data object Network : AppError

    /** El servidor respondió con un código fuera de 2xx. */
    data class Http(val code: Int) : AppError

    /** La respuesta llegó, pero el JSON no cumple el contrato del DTO. */
    data object Parse : AppError

    /** Room no pudo leer o escribir la base local. */
    data object Database : AppError
}
