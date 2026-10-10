package pe.edu.esan.loteria.domain

/** Estados posibles del sorteo, tal como se guardan en Firestore. */
enum class EstadoSorteo(val valor: String, val etiqueta: String) {
    ABIERTO("abierto", "Abierto"),
    EN_CURSO("en_curso", "En curso"),
    FINALIZADO("finalizado", "Finalizado");

    companion object {
        /** Acepta "en_curso", "en curso" o "EN CURSO" para facilitar la edición desde la consola. */
        fun desde(valor: String?): EstadoSorteo {
            val normal = valor?.trim()?.lowercase()?.replace(' ', '_')
            return entries.firstOrNull { it.valor == normal } ?: ABIERTO
        }
    }
}

/** Cabecera del sorteo: sorteos/{id} */
data class Sorteo(
    val id: String,
    val nombre: String,
    val fecha: String,
    val estado: EstadoSorteo
)

/** Bolilla publicada: sorteos/{id}/bolillas/{autoId} */
data class Bolilla(
    val numero: Int,
    val orden: Int
)

/** Jugada del usuario: sorteos/{id}/jugadas/{uid} (una por usuario). */
data class Jugada(
    val sorteoId: String,
    val uid: String,
    val numeros: List<Int>
)
