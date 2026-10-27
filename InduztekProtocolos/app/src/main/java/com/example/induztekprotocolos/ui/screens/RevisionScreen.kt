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
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.ChipResultado
import com.example.induztekprotocolos.ui.components.MensajeVacio
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens

/** Bandeja del ingeniero / jefe técnico: protocolos "Registrado en terreno" esperando formalizar u observar. */
@Composable
fun RevisionScreen(pendientes: List<ProtocoloVista>, onVolver: () -> Unit, onAbrir: (Long) -> Unit) {
    Scaffold(
        topBar = { BarraSuperior(titulo = "Revisión de protocolos", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (pendientes.isEmpty()) {
            MensajeVacio("No hay protocolos pendientes de revisión.", modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            itemsIndexed(pendientes, key = { _, v -> v.protocolo.id }) { indice, v ->
                AparecerAnimado(indice) {
                    Card(
                        onClick = { onAbrir(v.protocolo.id) },
                        shape = EstilosTarjeta.forma,
                        colors = EstilosTarjeta.colores(),
                        elevation = EstilosTarjeta.elevacion(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(v.protocolo.numero, style = MaterialTheme.typography.titleMedium)
                                ChipResultado(v.global)
                            }
                            Text("${v.equipo.tag} · ${v.equipo.tipo.etiqueta}", style = MaterialTheme.typography.bodyMedium)
                            Text("${v.pruebas.size} prueba(s) · ${Formato.fechaHora(v.protocolo.fechaHoraPrueba)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
