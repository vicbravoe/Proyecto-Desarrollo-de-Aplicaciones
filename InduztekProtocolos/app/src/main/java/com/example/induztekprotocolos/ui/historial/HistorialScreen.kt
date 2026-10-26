package cl.induztek.protocolos.ui.historial

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import cl.induztek.protocolos.data.local.EstadoProtocolo
import cl.induztek.protocolos.data.local.ProtocoloEntity
import cl.induztek.protocolos.data.local.ResultadoPrueba
import cl.induztek.protocolos.ui.theme.Dimens
import cl.induztek.protocolos.ui.theme.StatusAlert
import cl.induztek.protocolos.ui.theme.StatusDone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        uiState.equipo?.let { equipo ->
            Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                Text("${equipo.codigo} · ${equipo.tipo.etiqueta}", style = MaterialTheme.typography.titleMedium)
                Text(equipo.instalacion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (uiState.protocolos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Este equipo todavía no tiene pruebas registradas",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Dimens.espacioMedio)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(uiState.protocolos.size) { index ->
                    val protocolo = uiState.protocolos[index]
                    val anterior = uiState.protocolos.getOrNull(index + 1) // el siguiente en la lista = más antiguo
                    TarjetaProtocolo(protocolo = protocolo, anterior = anterior)
                }
            }
        }
    }
}

@Composable
private fun TarjetaProtocolo(protocolo: ProtocoloEntity, anterior: ProtocoloEntity?) {
    val formato = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL")) }
    val colorResultado = when (protocolo.resultado) {
        ResultadoPrueba.APROBADO -> StatusDone
        ResultadoPrueba.OBSERVADO -> MaterialTheme.colorScheme.primary
        ResultadoPrueba.RECHAZADO -> StatusAlert
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(formato.format(Date(protocolo.fechaHora)), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(protocolo.resultado.etiqueta, style = MaterialTheme.typography.labelMedium, color = colorResultado)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Dimens.espacioMini)) {
                Text(
                    "${protocolo.valorMedicion} ${protocolo.unidadMedicion}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (anterior != null) {
                    val icono = when {
                        protocolo.valorMedicion > anterior.valorMedicion -> Icons.Filled.ArrowUpward
                        protocolo.valorMedicion < anterior.valorMedicion -> Icons.Filled.ArrowDownward
                        else -> Icons.Filled.Remove
                    }
                    Icon(icono, contentDescription = "Comparado con la prueba anterior", modifier = Modifier.padding(start = Dimens.espacioMini).size(18.dp))
                }
            }

            if (protocolo.observaciones.isNotBlank()) {
                Text(protocolo.observaciones, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Dimens.espacioMini))
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Dimens.espacioChico)) {
                if (protocolo.latitud != null) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Con ubicación registrada", modifier = Modifier.size(16.dp))
                }
                Text(
                    if (protocolo.estado == EstadoProtocolo.FORMALIZADO) "  Formalizado" else "  Registrado en terreno",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            protocolo.fotoEvidencia?.let { bytes ->
                val bitmap = remember(bytes) {
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                }
                Image(bitmap = bitmap, contentDescription = "Evidencia fotográfica", modifier = Modifier.padding(top = Dimens.espacioChico).size(90.dp))
            }
        }
    }
}