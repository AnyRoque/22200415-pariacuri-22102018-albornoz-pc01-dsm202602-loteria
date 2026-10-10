package pe.edu.esan.loteria.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Barra superior con el correo del usuario autenticado, volver y cerrar sesión. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSesion(
    titulo: String,
    email: String?,
    onVolver: (() -> Unit)?,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(email ?: "", style = MaterialTheme.typography.bodySmall)
            }
        },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = { TextButton(onClick = onLogout) { Text("Salir") } }
    )
}

/** Círculo con un número (bolilla o número de la jugada). */
@Composable
fun Bola(
    numero: Int,
    resaltada: Boolean,
    modifier: Modifier = Modifier,
    tam: Dp = 44.dp,
    colorResaltado: Color = MaterialTheme.colorScheme.secondary
) {
    val fondo = if (resaltada) colorResaltado else MaterialTheme.colorScheme.surfaceVariant
    val texto = if (resaltada) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(tam)
            .clip(CircleShape)
            .background(fondo)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            numero.toString(),
            color = texto,
            fontWeight = FontWeight.Bold,
            fontSize = if (tam < 40.dp) 13.sp else 16.sp
        )
    }
}
