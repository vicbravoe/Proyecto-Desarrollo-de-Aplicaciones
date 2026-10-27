package com.example.induztekprotocolos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.ui.styles.EstilosCampo
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.Exito

// Componentes propios de Induztek, construidos igual que los de clases (un archivo = una pieza reutilizable).

/** Barra superior con el color primario, título y flecha para volver (si se entrega onVolver). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                }
            }
        },
        actions = { acciones() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/** Etiqueta de color genérica (la animación del color hace la transición al cambiar de estado). */
@Composable
fun Etiqueta(texto: String, colorFondo: Color, modifier: Modifier = Modifier) {
    val fondo by animateColorAsState(targetValue = colorFondo, label = "colorEtiqueta")
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun colorDeEstado(estado: EstadoProtocolo): Color = when (estado) {
    EstadoProtocolo.BORRADOR -> Color(0xFF7A7F87)
    EstadoProtocolo.REGISTRADO_TERRENO -> MaterialTheme.colorScheme.secondary
    EstadoProtocolo.OBSERVADO -> Color(0xFFC77D12)
    EstadoProtocolo.FORMALIZADO -> Exito
    EstadoProtocolo.ANULADO -> MaterialTheme.colorScheme.tertiary
}

@Composable
fun ChipEstado(estado: EstadoProtocolo, modifier: Modifier = Modifier) =
    Etiqueta(estado.etiqueta, colorDeEstado(estado), modifier)

@Composable
fun colorDeResultado(r: ResultadoPrueba?): Color = when (r) {
    ResultadoPrueba.APROBADO -> Exito
    ResultadoPrueba.OBSERVADO -> Color(0xFFC77D12)
    ResultadoPrueba.RECHAZADO -> MaterialTheme.colorScheme.tertiary
    null -> Color(0xFF7A7F87)
}

@Composable
fun ChipResultado(r: ResultadoPrueba?, modifier: Modifier = Modifier) =
    Etiqueta(r?.etiqueta ?: "Pendiente", colorDeResultado(r), modifier)

/** Una línea "Etiqueta ........ valor". */
@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End, modifier = Modifier.padding(start = 12.dp))
    }
}

/** Pantalla/lista vacía: un texto centrado. */
@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(Dimens.espacioGrande), contentAlignment = Alignment.Center) {
        Text(texto, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

/** Fila con ícono a la izquierda, título y subtítulo: se usa en menús y listas. */
@Composable
fun FilaIcono(@DrawableRes icono: Int, titulo: String, subtitulo: String? = null, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(icono), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Column(modifier = Modifier.weight(1f).padding(start = Dimens.espacioMedio)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            if (subtitulo != null) Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
        Icon(painter = painterResource(R.drawable.ic_chevron), contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

/** Lista desplegable genérica (mismo patrón ExposedDropdownMenuBox de FormularioScreen en clases). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    textoDe: (T) -> String,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { if (habilitado) expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionada?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}
