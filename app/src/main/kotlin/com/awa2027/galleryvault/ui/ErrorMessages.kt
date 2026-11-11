package com.awa2027.galleryvault.ui

import com.awa2027.galleryvault.domain.AppError

/** Texto para la persona usuaria. El `when` es exhaustivo: un AppError nuevo no compila sin mensaje. */
fun AppError.userMessage(): String = when (this) {
    AppError.Network -> "Sin conexión. Revisa tu red e intenta de nuevo."

    is AppError.Http -> if (code >= 500) {
        "El servidor del museo no responde (HTTP $code)."
    } else {
        "La API rechazó la solicitud (HTTP $code)."
    }

    AppError.Parse -> "La API respondió con un formato inesperado."

    AppError.Database -> "No se pudo leer la bóveda local."
}
