package pe.edu.esan.loteria.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/** Encapsula Firebase Authentication (correo y contraseña). */
object AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val usuarioActual: FirebaseUser? get() = auth.currentUser

    suspend fun registrar(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("No se pudo crear la cuenta")
    }.mapError()

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("No se pudo iniciar sesión")
    }.mapError()

    fun logout() = auth.signOut()

    /** Traduce los códigos de error de Firebase a mensajes en español. */
    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { e ->
        val code = (e as? FirebaseAuthException)?.errorCode
        val msg = when (code) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo"
            "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
                "Correo o contraseña incorrectos"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos, intenta más tarde"
            else -> e.message ?: "Error de autenticación"
        }
        throw Exception(msg)
    }
}
