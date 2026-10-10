package pe.edu.esan.loteria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LoteriaCalculoTest {

    private val jugadaA = listOf(5, 12, 18, 23, 34, 41)
    private val jugadaB = listOf(5, 12, 18, 23, 34, 42)
    private val salida = listOf(5, 12, 18, 23, 34, 41).mapIndexed { i, n -> Bolilla(n, i + 1) }

    // ---------- Validaciones ----------
    @Test fun jugadaValida() = assertNull(LoteriaCalculo.validarJugada(jugadaA))
    @Test fun menosDeSeis() = assertNotNull(LoteriaCalculo.validarJugada(listOf(1, 2, 3)))
    @Test fun masDeSeis() = assertNotNull(LoteriaCalculo.validarJugada(listOf(1, 2, 3, 4, 5, 6, 7)))
    @Test fun repetidos() = assertNotNull(LoteriaCalculo.validarJugada(listOf(1, 1, 3, 4, 5, 6)))
    @Test fun fueraDeRango() {
        assertNotNull(LoteriaCalculo.validarJugada(listOf(0, 2, 3, 4, 5, 6)))
        assertNotNull(LoteriaCalculo.validarJugada(listOf(1, 2, 3, 4, 5, 46)))
    }
    @Test fun nulo() = assertNotNull(LoteriaCalculo.validarJugada(listOf(1, 2, 3, 4, 5, null)))

    // ---------- Indicadores ----------
    @Test fun sinBolillas() {
        assertEquals(0, LoteriaCalculo.bolillasPublicadas(emptyList()))
        assertEquals(0, LoteriaCalculo.aciertos(jugadaA, emptyList()))
    }

    @Test fun progresoBolillaPorBolilla() {
        for (k in 1..5) {
            val parcial = salida.take(k)
            assertEquals(k, LoteriaCalculo.bolillasPublicadas(parcial))
            assertEquals(k, LoteriaCalculo.aciertos(jugadaA, parcial))
            assertEquals(k, LoteriaCalculo.aciertos(jugadaB, parcial))
        }
    }

    @Test fun ordenNoAfecta() {
        assertEquals(6, LoteriaCalculo.aciertos(jugadaA.reversed(), salida.reversed()))
        assertEquals(listOf(1, 2, 3, 4, 5, 6), LoteriaCalculo.ordenar(salida.shuffled()).map { it.orden })
    }

    // ---------- Resultado ----------
    @Test fun demostracionObligatoria() {
        val aA = LoteriaCalculo.aciertos(jugadaA, salida)
        val aB = LoteriaCalculo.aciertos(jugadaB, salida)
        assertEquals(6, aA)
        assertEquals(5, aB)
        assertEquals(Resultado.GANADOR, LoteriaCalculo.resultado(EstadoSorteo.FINALIZADO, 6, aA))
        assertEquals(Resultado.SIGUE_INTENTANDO, LoteriaCalculo.resultado(EstadoSorteo.FINALIZADO, 6, aB))
    }

    @Test fun sinResultadoAntesDeFinalizar() {
        assertEquals(Resultado.ESPERANDO_INICIO, LoteriaCalculo.resultado(EstadoSorteo.ABIERTO, 0, 0))
        assertEquals(Resultado.SORTEO_EN_CURSO, LoteriaCalculo.resultado(EstadoSorteo.EN_CURSO, 6, 6))
        assertEquals(Resultado.ESPERANDO_RESULTADOS, LoteriaCalculo.resultado(EstadoSorteo.FINALIZADO, 5, 5))
    }

    @Test fun estadoDesdeConsola() {
        assertEquals(EstadoSorteo.EN_CURSO, EstadoSorteo.desde("en curso"))
        assertEquals(EstadoSorteo.EN_CURSO, EstadoSorteo.desde("en_curso"))
        assertEquals(EstadoSorteo.FINALIZADO, EstadoSorteo.desde("Finalizado"))
    }
}
