package pe.edu.esan.loteria.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginOk: () -> Unit,
    onIrRegistro: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(s.exito) {
        if (s.exito) { vm.consumirExito(); onLoginOk() }
    }
    FormularioAuth(titulo = "Iniciar sesión") {
        CamposCredenciales(s, vm)
        MensajeError(s.error)
        BotonPrincipal("Ingresar", s.cargando, vm::login)
        TextButton(onClick = onIrRegistro) { Text("¿No tienes cuenta? Regístrate") }
    }
}

@Composable
fun RegistroScreen(
    onRegistroOk: () -> Unit,
    onIrLogin: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(s.exito) {
        if (s.exito) { vm.consumirExito(); onRegistroOk() }
    }
    FormularioAuth(titulo = "Crear cuenta") {
        CamposCredenciales(s, vm)
        OutlinedTextField(
            value = s.confirmacion,
            onValueChange = vm::onConfirmacion,
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        MensajeError(s.error)
        BotonPrincipal("Registrarme", s.cargando, vm::registrar)
        TextButton(onClick = onIrLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}

@Composable
private fun FormularioAuth(titulo: String, contenido: @Composable () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lotería PC01", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            contenido()
        }
    }
}

@Composable
private fun CamposCredenciales(s: AuthUiState, vm: AuthViewModel) {
    OutlinedTextField(
        value = s.email,
        onValueChange = vm::onEmail,
        label = { Text("Correo") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = s.password,
        onValueChange = vm::onPassword,
        label = { Text("Contraseña") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MensajeError(error: String?) {
    if (error != null) {
        Text(error, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
        if (cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(texto)
    }
}
