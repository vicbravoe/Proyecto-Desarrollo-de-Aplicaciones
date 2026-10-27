package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.ChipEstado
import com.example.induztekprotocolos.ui.components.ChipResultado
import com.example.induztekprotocolos.ui.components.Etiqueta
import com.example.induztekprotocolos.ui.components.MensajeVacio
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens

/** RF-07 / HU de historial: protocolos anteriores del equipo y cómo varió cada valor (RN-09). */
@Composable
fun HistorialScreen(
    nombreEquipo: String,
    protocolos: List<ProtocoloVista>,
    onVolver: () -> Unit,
    onAbrirProtocolo: ((Long) -> Unit)? = null
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = "Historial", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (protocolos.isEmpty()) {
            MensajeVacio("$nombreEquipo no tiene protocolos para mostrar.", modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item { Text(nombreEquipo, style = MaterialTheme.typography.titleLarge) }
            itemsIndexed(protocolos, key = { _, v -> v.protocolo.id }) { indice, v ->
                AparecerAnimado(indice) {
                    Card(
                        onClick = { onAbrirProtocolo?.invoke(v.protocolo.id) },
                        enabled = onAbrirProtocolo != null,
                        shape = EstilosTarjeta.forma,
                        colors = EstilosTarjeta.colores(),
                        elevation = EstilosTarjeta.elevacion(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(v.protocolo.numero, style = MaterialTheme.typography.titleMedium)
                                ChipEstado(v.protocolo.estado)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                Text(Formato.fecha(v.protocolo.fechaHoraPrueba), style = MaterialTheme.typography.bodySmall)
                                ChipResultado(v.global)
                            }
                            v.pruebas.filter { it.prueba.realizada }.forEach { p ->
                                Column(modifier = Modifier.padding(top = Dimens.espacioMini)) {
                                    Text("${p.prueba.tipoPrueba.etiqueta} · ${p.prueba.elemento}", style = MaterialTheme.typography.bodyMedium)
                                    val actual = p.valorActual
                                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                        Text(
                                            actual?.let { "Valor: ${Formato.numero(it)}" } ?: "Sin valor",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        p.variacionPct?.let { Text("vs. anterior ${Formato.porcentaje(it)}", style = MaterialTheme.typography.bodySmall) }
                                    }
                                    if (p.significativa) Etiqueta("Variación significativa", Color(0xFFC77D12))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
