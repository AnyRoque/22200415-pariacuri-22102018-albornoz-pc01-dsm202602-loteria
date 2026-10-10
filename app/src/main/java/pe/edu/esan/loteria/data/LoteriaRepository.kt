package pe.edu.esan.loteria.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.edu.esan.loteria.domain.Bolilla
import pe.edu.esan.loteria.domain.EstadoSorteo
import pe.edu.esan.loteria.domain.Jugada
import pe.edu.esan.loteria.domain.Sorteo
import java.time.LocalDate

/**
 * Estructura en Firestore:
 *  sorteos/{sorteoId}                      -> nombre, fecha, estado
 *  sorteos/{sorteoId}/bolillas/{autoId}    -> numero, orden
 *  sorteos/{sorteoId}/jugadas/{uid}        -> sorteoId, uid, email, numeros[6], registradoEn
 */
object LoteriaRepository {

    const val SORTEO_ID = "sorteo-prueba"
    private const val TAG = "LoteriaListener"

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private fun sorteoRef(id: String) = db.collection("sorteos").document(id)

    /** Crea la cabecera del sorteo de prueba si aún no existe (estado abierto). */
    suspend fun asegurarSorteo(id: String = SORTEO_ID) {
        val ref = sorteoRef(id)
        if (!ref.get().await().exists()) {
            ref.set(
                mapOf(
                    "nombre" to "Sorteo de prueba PC01",
                    "fecha" to LocalDate.now().toString(),
                    "estado" to EstadoSorteo.ABIERTO.valor
                )
            ).await()
        }
    }

    // ---------- Escucha en tiempo real (addSnapshotListener + callbackFlow) ----------
    // awaitClose { remove() } garantiza que el listener se elimina cuando nadie observa el Flow.

    fun escucharSorteo(id: String = SORTEO_ID): Flow<Sorteo?> = callbackFlow {
        Log.d(TAG, "ON  sorteo")
        val reg = sorteoRef(id).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snap?.toSorteo())
        }
        awaitClose { Log.d(TAG, "OFF sorteo"); reg.remove() }
    }

    fun escucharBolillas(id: String = SORTEO_ID): Flow<List<Bolilla>> = callbackFlow {
        Log.d(TAG, "ON  bolillas")
        val reg = sorteoRef(id).collection("bolillas")
            .orderBy("orden", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snap?.documents?.mapNotNull { d ->
                    val numero = (d.get("numero") as? Number)?.toInt()
                    val orden = (d.get("orden") as? Number)?.toInt()
                    if (numero != null && orden != null) Bolilla(numero, orden) else null
                }.orEmpty()
                trySend(lista)
            }
        awaitClose { Log.d(TAG, "OFF bolillas"); reg.remove() }
    }

    fun escucharJugada(uid: String, id: String = SORTEO_ID): Flow<Jugada?> = callbackFlow {
        Log.d(TAG, "ON  jugada")
        val reg = sorteoRef(id).collection("jugadas").document(uid)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snap?.toJugada())
            }
        awaitClose { Log.d(TAG, "OFF jugada"); reg.remove() }
    }

    /**
     * Registra (o reemplaza) la jugada del usuario SOLO si el sorteo está abierto.
     * Se usa una transacción para leer el estado y escribir de forma atómica.
     */
    suspend fun registrarJugada(uid: String, email: String?, numeros: List<Int>, id: String = SORTEO_ID) {
        val ref = sorteoRef(id)
        db.runTransaction { tx ->
            val estado = EstadoSorteo.desde(tx.get(ref).getString("estado"))
            if (estado != EstadoSorteo.ABIERTO) {
                throw IllegalStateException("El sorteo ya inició: no se pueden registrar ni modificar jugadas")
            }
            tx.set(
                ref.collection("jugadas").document(uid),
                mapOf(
                    "sorteoId" to id,
                    "uid" to uid,
                    "email" to email,
                    "numeros" to numeros.sorted().map { it.toLong() },
                    "registradoEn" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    private fun DocumentSnapshot.toSorteo(): Sorteo? =
        if (!exists()) null else Sorteo(
            id = id,
            nombre = getString("nombre") ?: id,
            fecha = getString("fecha") ?: "",
            estado = EstadoSorteo.desde(getString("estado"))
        )

    private fun DocumentSnapshot.toJugada(): Jugada? {
        if (!exists()) return null
        val numeros = (get("numeros") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return null
        return Jugada(
            sorteoId = getString("sorteoId") ?: "",
            uid = getString("uid") ?: id,
            numeros = numeros
        )
    }
}
