package pe.edu.esan.loteria.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.esan.loteria.data.AuthRepository
import pe.edu.esan.loteria.ui.auth.LoginScreen
import pe.edu.esan.loteria.ui.auth.RegistroScreen
import pe.edu.esan.loteria.ui.jugada.JugadaScreen
import pe.edu.esan.loteria.ui.seguimiento.SeguimientoScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val JUGADA = "jugada"
    const val SEGUIMIENTO = "seguimiento"
}

@Composable
fun AppNavGraph(nav: NavHostController = rememberNavController()) {
    // Si la sesión sigue activa (persistencia de Firebase Auth) se entra directo a la jugada.
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
            JugadaScreen(
                onVerSeguimiento = { nav.navigate(Rutas.SEGUIMIENTO) { launchSingleTop = true } },
                onLogout = logout
            )
        }
        composable(Rutas.SEGUIMIENTO) {
            SeguimientoScreen(
                onVolver = { nav.popBackStack() },
                onLogout = logout
            )
        }
    }
}
