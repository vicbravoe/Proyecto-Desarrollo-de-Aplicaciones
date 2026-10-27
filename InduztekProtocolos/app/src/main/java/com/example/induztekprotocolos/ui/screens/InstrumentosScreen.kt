package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.Validaciones
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Res
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.components.Etiqueta
import com.example.induztekprotocolos.ui.components.MensajeVacio
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.Exito

private const val DIA_MS = 24L * 60L * 60L * 1000L

/** RF-08 / RN-11: instrumentos con su calibración. Los vencidos no se pueden usar en una prueba. */
@Composable
fun InstrumentosScreen(
    instrumentos: List<Instrumento>,
    puedeGestionar: Boolean,
    onVolver: () -> Unit,
    onRegistrar: (nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long) -> Res<Long>
) {
    var dialogo by remember { mutableStateOf(false) }
    if (dialogo) DialogoInstrumento(onCancelar = { dialogo = false }, onGuardar = { n, m, mo, s, c, v ->
        val r = onRegistrar(n, m, mo, s, c, v)
        if (r.ok) dialogo = false
        r.error
    })

    Scaffold(
        topBar = { BarraSuperior(titulo = "Instrumentos", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (puedeGestionar) {
                BotonPrincipal(
                    texto = "Registrar instrumento",
                    icono = R.drawable.ic_agregar,
                    onClick = { dialogo = true },
                    modifier = Modifier.fillMaxWidth().padding(Dimens.espacioPantalla)
                )
            }
            if (instrumentos.isEmpty()) {
                MensajeVacio("No hay instrumentos registrados.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    itemsIndexed(instrumentos, key = { _, i -> i.id }) { indice, i ->
                        AparecerAnimado(indice) {
                            val dias = (i.fechaVencimiento - System.currentTimeMillis()) / DIA_MS
                            Card(
                                shape = EstilosTarjeta.forma,
                                colors = EstilosTarjeta.colores(),
                                elevation = EstilosTarjeta.elevacion(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(i.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        when {
                                            i.fechaVencimiento < System.currentTimeMillis() -> Etiqueta("Vencido", MaterialTheme.colorScheme.tertiary)
                                            dias <= 30 -> Etiqueta("Vence en $dias día(s)", Color(0xFFC77D12))
                                            else -> Etiqueta("Vigente", Exito)
                                        }
                                    }
                                    Text("${i.marca} ${i.modelo} · serie ${i.numeroSerie}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Calibrado ${Formato.fecha(i.fechaUltimaCalibracion)} · vence ${Formato.fecha(i.fechaVencimiento)}",
                                        style = MaterialTheme.typography.bodySmall
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

@Composable
private fun DialogoInstrumento(
    onCancelar: () -> Unit,
    onGuardar: (nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long) -> String?
) {
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var calibracion by remember { mutableStateOf("") }
    var vencimiento by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorCalibracion by remember { mutableStateOf<String?>(null) }
    var errorVencimiento by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Registrar instrumento") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                CampoTexto(nombre, { nombre = it; errorNombre = null }, "Nombre", R.drawable.ic_instrumento, error = errorNombre)
                CampoTexto(marca, { marca = it }, "Marca", R.drawable.ic_editar)
                CampoTexto(modelo, { modelo = it }, "Modelo", R.drawable.ic_editar)
                CampoTexto(serie, { serie = it }, "N° de serie", R.drawable.ic_etiqueta)
                CampoTexto(calibracion, { calibracion = it; errorCalibracion = null }, "Última calibración (dd-mm-aaaa)", R.drawable.ic_historial, error = errorCalibracion)
                CampoTexto(vencimiento, { vencimiento = it; errorVencimiento = null }, "Vencimiento (dd-mm-aaaa)", R.drawable.ic_historial, error = errorVencimiento)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errorNombre = Validaciones.nombre(nombre)
                val c = Validaciones.fecha(calibracion)
                val v = Validaciones.fecha(vencimiento)
                errorCalibracion = c.error
                errorVencimiento = v.error
                if (errorNombre == null && c.valor != null && v.valor != null) {
                    val mensaje = onGuardar(nombre, marca, modelo, serie, c.valor, v.valor)
                    if (mensaje != null) errorVencimiento = mensaje
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
