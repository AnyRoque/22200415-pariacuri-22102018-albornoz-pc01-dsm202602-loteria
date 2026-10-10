package pe.edu.esan.loteria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Rojo = Color(0xFFC8102E)
val Verde = Color(0xFF2E7D32)

private val Claro = lightColorScheme(primary = Rojo, secondary = Verde)
private val Oscuro = darkColorScheme(primary = Color(0xFFFF8A80), secondary = Color(0xFF81C784))

@Composable
fun LoteriaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        content = content
    )
}
