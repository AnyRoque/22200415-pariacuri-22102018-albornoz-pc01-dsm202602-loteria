package pe.edu.esan.loteria.ui.seguimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import pe.edu.esan.loteria.data.AuthRepository
import pe.edu.esan.loteria.data.LoteriaRepository
import pe.edu.esan.loteria.domain.Bolilla
import pe.edu.esan.loteria.domain.Jugada
import pe.edu.esan.loteria.domain.LoteriaCalculo
import pe.edu.esan.loteria.domain.Resultado
import pe.edu.esan.loteria.domain.Sorteo

data class SeguimientoUiState(
    val cargando: Boolean = true,
    val sorteo: Sorteo? = null,
    val bolillas: List<Bolilla> = emptyList(),
    val jugada: Jugada? = null,
    val publicadas: Int = 0,
    val aciertos: Int = 0,
    val acertados: Set<Int> = emptySet(),
    val resultado: Resultado = Resultado.ESPERANDO_INICIO,
    val error: String? = null
)

class SeguimientoViewModel : ViewModel() {

    private val uid = AuthRepository.usuarioActual?.uid
    val email: String? = AuthRepository.usuarioActual?.email

    /**
     * Combina 3 listeners en tiempo real (estado del sorteo, bolillas y jugada).
     * Cada vez que Firestore emite un cambio se recalculan aciertos y resultado.
     * WhileSubscribed: al salir de la pantalla se cancelan los Flows y awaitClose
     * ejecuta registration.remove(), por lo que no quedan listeners activos ni duplicados.
     */
    val state: StateFlow<SeguimientoUiState> =
        combine(
            LoteriaRepository.escucharSorteo(),
            LoteriaRepository.escucharBolillas(),
            if (uid != null) LoteriaRepository.escucharJugada(uid) else flowOf(null)
        ) { sorteo, bolillas, jugada ->
            val ordenadas = LoteriaCalculo.ordenar(bolillas)
            val numeros = jugada?.numeros.orEmpty()
            val publicadas = LoteriaCalculo.bolillasPublicadas(ordenadas)
            val aciertos = LoteriaCalculo.aciertos(numeros, ordenadas)
            SeguimientoUiState(
                cargando = false,
                sorteo = sorteo,
                bolillas = ordenadas,
                jugada = jugada,
                publicadas = publicadas,
                aciertos = aciertos,
                acertados = LoteriaCalculo.numerosAcertados(numeros, ordenadas),
                resultado = sorteo?.let { LoteriaCalculo.resultado(it.estado, publicadas, aciertos) }
                    ?: Resultado.ESPERANDO_INICIO
            )
        }
            .catch { e -> emit(SeguimientoUiState(cargando = false, error = e.message)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeguimientoUiState())
}
