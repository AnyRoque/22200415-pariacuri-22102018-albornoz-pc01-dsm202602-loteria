package pe.edu.esan.loteria.ui.seguimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.esan.loteria.domain.Resultado
import pe.edu.esan.loteria.ui.auth.MensajeError
import pe.edu.esan.loteria.ui.common.BarraSesion
import pe.edu.esan.loteria.ui.common.Bola
import pe.edu.esan.loteria.ui.jugada.TarjetaSorteo

@Composable
fun SeguimientoScreen(
    onVolver: () -> Unit,
    onLogout: () -> Unit,
    vm: SeguimientoViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSesion("Seguimiento", vm.email, onVolver = onVolver, onLogout = onLogout) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (s.cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                return@Column
            }
            MensajeError(s.error)
            TarjetaSorteo(s.sorteo)
            TarjetaResultado(s.resultado)

            Text("Bolillas publicadas: ${s.publicadas} de 6", style = MaterialTheme.typography.titleMedium)
            if (s.bolillas.isEmpty()) {
                Text("Aún no se publican bolillas.")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    s.bolillas.forEach { b ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Bola(b.numero, resaltada = b.numero in s.acertados)
                            Text("${b.orden}°", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Text("Tu jugada", style = MaterialTheme.typography.titleMedium)
            val jugada = s.jugada
            if (jugada == null) {
                Text("No registraste jugada para este sorteo.", color = MaterialTheme.colorScheme.error)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    jugada.numeros.sorted().forEach { Bola(it, resaltada = it in s.acertados) }
                }
                Text(
                    "Aciertos: ${s.aciertos}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver a mi jugada")
            }
        }
    }
}

@Composable
private fun TarjetaResultado(resultado: Resultado) {
    val (fondo, texto) = when (resultado) {
        Resultado.GANADOR -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        Resultado.SIGUE_INTENTANDO -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto)
    ) {
        Text(
            resultado.texto,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
