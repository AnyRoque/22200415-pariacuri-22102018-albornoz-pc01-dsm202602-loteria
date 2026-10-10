package pe.edu.esan.loteria.ui.jugada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.esan.loteria.data.AuthRepository
import pe.edu.esan.loteria.data.LoteriaRepository
import pe.edu.esan.loteria.domain.EstadoSorteo
import pe.edu.esan.loteria.domain.Jugada
import pe.edu.esan.loteria.domain.LoteriaCalculo
import pe.edu.esan.loteria.domain.Sorteo

data class JugadaUiState(
    val cargando: Boolean = true,
    val sorteo: Sorteo? = null,
    val jugada: Jugada? = null,
    val seleccion: Set<Int> = emptySet(),
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    val puedeRegistrar: Boolean get() = sorteo?.estado == EstadoSorteo.ABIERTO
}

private data class Local(
    val seleccion: Set<Int> = emptySet(),
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class JugadaViewModel : ViewModel() {

    private val uid = AuthRepository.usuarioActual?.uid
    val email: String? = AuthRepository.usuarioActual?.email

    private val local = MutableStateFlow(Local())

    init {
        viewModelScope.launch {
            runCatching { LoteriaRepository.asegurarSorteo() }
                .onFailure { e -> local.update { it.copy(error = e.message) } }
        }
    }

    private val sorteoFlow = LoteriaRepository.escucharSorteo()
        .map<Sorteo?, Result<Sorteo?>> { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    private val jugadaFlow = (if (uid != null) LoteriaRepository.escucharJugada(uid) else flowOf(null))
        .map<Jugada?, Result<Jugada?>> { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    /** Estado combinado; los listeners viven solo mientras la pantalla está suscrita. */
    val state: StateFlow<JugadaUiState> =
        combine(sorteoFlow, jugadaFlow, local) { s, j, l ->
            JugadaUiState(
                cargando = false,
                sorteo = s.getOrNull(),
                jugada = j.getOrNull(),
                seleccion = l.seleccion,
                guardando = l.guardando,
                error = l.error ?: s.exceptionOrNull()?.message ?: j.exceptionOrNull()?.message,
                mensaje = l.mensaje
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JugadaUiState())

    fun alternar(numero: Int) = local.update { l ->
        val nueva = when {
            numero in l.seleccion -> l.seleccion - numero
            l.seleccion.size >= LoteriaCalculo.CANTIDAD -> l.seleccion
            else -> l.seleccion + numero
        }
        val error = if (nueva == l.seleccion && numero !in l.seleccion)
            "Solo puedes elegir ${LoteriaCalculo.CANTIDAD} números" else null
        l.copy(seleccion = nueva, error = error, mensaje = null)
    }

    fun limpiar() = local.update { it.copy(seleccion = emptySet(), error = null, mensaje = null) }

    fun registrar() {
        val actual = state.value
        val numeros = local.value.seleccion.toList()
        val error = when {
            uid == null -> "Sesión no válida, vuelve a iniciar sesión"
            !actual.puedeRegistrar -> "El sorteo ya inició: no se pueden registrar ni modificar jugadas"
            else -> LoteriaCalculo.validarJugada(numeros)
        }
        if (error != null) {
            local.update { it.copy(error = error, mensaje = null) }
            return
        }
        local.update { it.copy(guardando = true, error = null, mensaje = null) }
        viewModelScope.launch {
            runCatching { LoteriaRepository.registrarJugada(uid!!, email, numeros) }
                .onSuccess {
                    local.update { it.copy(guardando = false, seleccion = emptySet(), mensaje = "Jugada registrada") }
                }
                .onFailure { e ->
                    local.update { it.copy(guardando = false, error = e.message ?: "No se pudo registrar") }
                }
        }
    }
}
