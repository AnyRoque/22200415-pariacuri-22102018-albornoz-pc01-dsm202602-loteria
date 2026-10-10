package pe.edu.esan.loteria.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.loteria.data.AuthRepository
import pe.edu.esan.loteria.ui.auth.LoginScreen
import pe.edu.esan.loteria.ui.auth.RegistroScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val JUGADA = "jugada"
}

@Composable
fun AppNavGraph(nav: NavHostController = rememberNavController()) {
    val inicio = if (AuthRepository.usuarioActual != null) Rutas.JUGADA else Rutas.LOGIN

    val irAJugada = {
        nav.navigate(Rutas.JUGADA) { popUpTo(nav.graph.id) { inclusive = true } }
    }
    val logout = {
        AuthRepository.logout()
        nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
    }

    NavHost(navController = nav, startDestination = inicio) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginOk = irAJugada,
                onIrRegistro = { nav.navigate(Rutas.REGISTRO) { launchSingleTop = true } }
            )
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroOk = irAJugada,
                onIrLogin = {
                    if (!nav.popBackStack(Rutas.LOGIN, inclusive = false)) {
                        nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
                    }
                }
            )
        }
        composable(Rutas.JUGADA) {
            // Temporal: la pantalla de jugada y seguimiento se agregan en el siguiente commit.
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sesión iniciada: ${AuthRepository.usuarioActual?.email}")
                Button(onClick = logout) { Text("Cerrar sesión") }
            }
        }
    }
}
