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
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.model.VisitaResumen
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.Etiqueta
import com.example.induztekprotocolos.ui.components.MensajeVacio
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.Exito

@Composable
fun VisitasScreen(
    visitas: List<VisitaResumen>,
    puedeCrear: Boolean,
    onVolver: () -> Unit,
    onNueva: () -> Unit,
    onAbrir: (Long) -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = "Visitas a terreno", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (puedeCrear) {
                BotonPrincipal(
                    texto = "Nueva visita",
                    icono = R.drawable.ic_agregar,
                    onClick = onNueva,
                    modifier = Modifier.fillMaxWidth().padding(Dimens.espacioPantalla)
                )
            }
            if (visitas.isEmpty()) {
                MensajeVacio("Aún no hay visitas. Crea la primera con «Nueva visita».")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    itemsIndexed(visitas, key = { _, v -> v.visita.id }) { indice, resumen ->
                        AparecerAnimado(indice) {
                            Card(
                                onClick = { onAbrir(resumen.visita.id) },
                                shape = EstilosTarjeta.forma,
                                colors = EstilosTarjeta.colores(),
                                elevation = EstilosTarjeta.elevacion(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(resumen.visita.numeroOt, style = MaterialTheme.typography.titleMedium)
                                        if (resumen.visita.cerrada) Etiqueta("Cerrada", Exito) else Etiqueta("Abierta", Color(0xFF2F6F8F))
                                    }
                                    Text(resumen.instalacion, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${Formato.fecha(resumen.visita.fecha)} · ${resumen.visita.tipoVisita.etiqueta}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "${resumen.totalProtocolos} protocolo(s) · ${resumen.formalizados} formalizado(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
