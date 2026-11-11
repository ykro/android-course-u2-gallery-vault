package com.awa2027.galleryvault.student

import com.awa2027.galleryvault.VaultTestEnv
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tus tests propios del reto. Tus tests propios suman si pasan contra la solución de referencia y
 * detectan mutantes que la suite pública no detecta (errores sembrados en el reto).
 * Reescribir un test público no suma.
 *
 * Reemplaza las dos plantillas por al menos 2 escenarios.
 * Puedes usar las utilidades de TestSupport.kt (VaultTestEnv, FakeVaultRepository, fixtures).
 * Encima de cada test, escribe qué bug concreto lo haría fallar.
 */
@RunWith(RobolectricTestRunner::class)
class MyScenariosTest {
    private val env = VaultTestEnv()

    @After
    fun tearDown() = env.close()

    @Test
    fun `escenario 1 del reto`() = runTest {
        fail("Escribe tu primer escenario del reto")
    }

    @Test
    fun `escenario 2 del reto`() = runTest {
        fail("Escribe tu segundo escenario del reto")
    }
}
