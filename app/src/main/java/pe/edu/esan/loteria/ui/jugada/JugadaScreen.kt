package pe.edu.esan.loteria.ui.jugada

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.domain.EstadoSorteo
import pe.edu.esan.loteria.domain.LoteriaCalculo
import pe.edu.esan.loteria.domain.Sorteo
import pe.edu.esan.loteria.ui.auth.MensajeError
import pe.edu.esan.loteria.ui.common.BarraSesion
import pe.edu.esan.loteria.ui.common.Bola

@Composable
fun JugadaScreen(
    onVerSeguimiento: () -> Unit,
    onLogout: () -> Unit,
    vm: JugadaViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSesion("Mi jugada", vm.email, onVolver = null, onLogout = onLogout) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (s.cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }
            TarjetaSorteo(s.sorteo)

            val jugada = s.jugada
            if (jugada != null) {
                Text("Tu jugada registrada", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    jugada.numeros.sorted().forEach { Bola(it, resaltada = true, colorResaltado = MaterialTheme.colorScheme.primary) }
                }
            } else if (!s.puedeRegistrar) {
                Text("El sorteo ya inició y no registraste jugada.", color = MaterialTheme.colorScheme.error)
            }

            if (s.puedeRegistrar) {
                Text(
                    if (jugada == null) "Elige 6 números del 1 al 45 (${s.seleccion.size}/6)"
                    else "Puedes cambiar tu jugada mientras el sorteo esté abierto (${s.seleccion.size}/6)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Grilla(s.seleccion, vm::alternar)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::limpiar, enabled = !s.guardando) { Text("Limpiar") }
                    Button(
                        onClick = vm::registrar,
                        enabled = !s.guardando && s.seleccion.size == LoteriaCalculo.CANTIDAD
                    ) { Text(if (jugada == null) "Registrar jugada" else "Actualizar jugada") }
                }
            }

            MensajeError(s.error)
            s.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }

            Button(
                onClick = onVerSeguimiento,
                enabled = s.sorteo != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ver seguimiento del sorteo →") }
        }
    }
}

@Composable
fun TarjetaSorteo(sorteo: Sorteo?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            if (sorteo == null) {
                Text("Cargando sorteo…")
            } else {
                Text(sorteo.nombre, style = MaterialTheme.typography.titleLarge)
                Text("Fecha: ${sorteo.fecha}")
                Text(
                    "Estado: ${sorteo.estado.etiqueta}",
                    color = when (sorteo.estado) {
                        EstadoSorteo.ABIERTO -> MaterialTheme.colorScheme.secondary
                        EstadoSorteo.EN_CURSO -> MaterialTheme.colorScheme.primary
                        EstadoSorteo.FINALIZADO -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun Grilla(seleccion: Set<Int>, onClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LoteriaCalculo.RANGO.toList().chunked(8).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                fila.forEach { n ->
                    Bola(
                        numero = n,
                        resaltada = n in seleccion,
                        tam = 34.dp,
                        colorResaltado = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onClick(n) }
                    )
                }
            }
        }
    }
}
