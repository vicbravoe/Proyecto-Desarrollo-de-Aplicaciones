package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.FilaIcono
import com.example.induztekprotocolos.ui.components.MensajeVacio
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens

/** HU del usuario cliente (RN: solo ve lo de su empresa y solo protocolos Formalizados). */
@Composable
fun ClienteScreen(
    nombreEmpresa: String,
    instalaciones: List<Instalacion>,
    equiposDe: (Long) -> List<Equipo>,
    onVolver: () -> Unit,
    onHistorial: (idEquipo: Long) -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = "Mis equipos", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (instalaciones.isEmpty()) {
            MensajeVacio("Tu empresa aún no tiene instalaciones registradas.", modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(nombreEmpresa, style = MaterialTheme.typography.titleLarge)
            instalaciones.forEach { inst ->
                TarjetaSeccion(titulo = "${inst.nombre} (${inst.comuna})") {
                    val equipos = equiposDe(inst.id)
                    if (equipos.isEmpty()) Text("Sin equipos registrados.", style = MaterialTheme.typography.bodySmall)
                    equipos.forEach { e ->
                        Card(
                            onClick = { onHistorial(e.id) },
                            shape = EstilosTarjeta.forma,
                            colors = EstilosTarjeta.colores(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Dimens.espacioChico)) {
                                FilaIcono(R.drawable.ic_historial, e.tag, "${e.tipo.etiqueta} · ver historial")
                            }
                        }
                    }
                }
            }
        }
    }
}
