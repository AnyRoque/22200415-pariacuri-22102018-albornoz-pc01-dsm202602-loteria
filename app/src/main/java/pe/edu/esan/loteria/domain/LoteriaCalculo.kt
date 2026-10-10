package pe.edu.esan.loteria.domain

/** Resultado que la app determina (nunca se guarda en Firestore). */
enum class Resultado(val texto: String) {
    ESPERANDO_INICIO("Esperando el inicio del sorteo…"),
    SORTEO_EN_CURSO("Sorteo en curso"),
    ESPERANDO_RESULTADOS("Esperando resultados…"),
    GANADOR("GANADOR"),
    SIGUE_INTENTANDO("SIGUE INTENTANDO")
}

/**
 * Lógica pura (sin Android ni Firebase) para validar jugadas y calcular aciertos.
 * Se prueba con tests unitarios en src/test.
 */
object LoteriaCalculo {

    const val CANTIDAD = 6
    val RANGO = 1..45

    /** Devuelve null si la jugada es válida o el mensaje de error. */
    fun validarJugada(numeros: List<Int?>): String? = when {
        numeros.size != CANTIDAD -> "Debes elegir exactamente $CANTIDAD números"
        numeros.any { it == null } -> "Todos los números deben ser enteros"
        numeros.any { it !in RANGO } -> "Los números deben estar entre ${RANGO.first} y ${RANGO.last}"
        numeros.toSet().size != CANTIDAD -> "Los números no se pueden repetir"
        else -> null
    }

    /** Bolillas válidas, sin duplicados de orden, ordenadas por orden de salida. */
    fun ordenar(bolillas: List<Bolilla>): List<Bolilla> =
        bolillas
            .filter { it.numero in RANGO && it.orden in 1..CANTIDAD }
            .distinctBy { it.orden }
            .sortedBy { it.orden }

    /** Indicador "Bolillas publicadas": 0 a 6. */
    fun bolillasPublicadas(bolillas: List<Bolilla>): Int =
        ordenar(bolillas).size.coerceIn(0, CANTIDAD)

    /** Números de la jugada que salieron (el orden no importa). */
    fun numerosAcertados(jugada: List<Int>, bolillas: List<Bolilla>): Set<Int> {
        val sorteados = ordenar(bolillas).map { it.numero }.toSet()
        return jugada.toSet().intersect(sorteados)
    }

    /** Indicador "Aciertos": cantidad de números distintos de la jugada presentes entre las bolillas. */
    fun aciertos(jugada: List<Int>, bolillas: List<Bolilla>): Int =
        numerosAcertados(jugada, bolillas).size

    /**
     * Resultado final: solo se define si el sorteo está FINALIZADO y llegaron las 6 bolillas.
     */
    fun resultado(estado: EstadoSorteo, publicadas: Int, aciertos: Int): Resultado = when (estado) {
        EstadoSorteo.ABIERTO -> Resultado.ESPERANDO_INICIO
        EstadoSorteo.EN_CURSO -> Resultado.SORTEO_EN_CURSO
        EstadoSorteo.FINALIZADO ->
            if (publicadas < CANTIDAD) Resultado.ESPERANDO_RESULTADOS
            else if (aciertos == CANTIDAD) Resultado.GANADOR
            else Resultado.SIGUE_INTENTANDO
    }
}
