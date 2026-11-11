package com.awa2027.galleryvault.data

import com.awa2027.galleryvault.domain.AppError

/**
 * Taller, paso 2. Traduce excepciones de Retrofit, kotlinx.serialization y Room a [AppError].
 * Cualquier otra excepción es un bug de programación: se relanza para que no quede oculta.
 */
fun Throwable.toAppError(): AppError = TODO("Taller, paso 2: traducir la excepción a AppError")
