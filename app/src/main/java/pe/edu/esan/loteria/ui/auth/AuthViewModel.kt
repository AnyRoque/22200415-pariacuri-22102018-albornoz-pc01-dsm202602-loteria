package pe.edu.esan.loteria.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.esan.loteria.data.AuthRepository

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val exito: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmail(v: String) = _state.update { it.copy(email = v.trim(), error = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmacion(v: String) = _state.update { it.copy(confirmacion = v, error = null) }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Ingresa correo y contraseña") }
            return
        }
        ejecutar { AuthRepository.login(s.email, s.password) }
    }

    fun registrar() {
        val s = _state.value
        val error = when {
            s.email.isBlank() || s.password.isBlank() -> "Ingresa correo y contraseña"
            s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            s.password != s.confirmacion -> "Las contraseñas no coinciden"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        ejecutar { AuthRepository.registrar(s.email, s.password) }
    }

    private fun ejecutar(accion: suspend () -> Result<*>) {
        _state.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            accion()
                .onSuccess { _state.update { it.copy(cargando = false, exito = true) } }
                .onFailure { e -> _state.update { it.copy(cargando = false, error = e.message) } }
        }
    }

    fun consumirExito() = _state.update { it.copy(exito = false) }
}
