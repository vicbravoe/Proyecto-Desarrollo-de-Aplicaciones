package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.Validaciones
import com.example.induztekprotocolos.model.DetalleVisita
import com.example.induztekprotocolos.model.Res
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.BotonSecundario
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.components.ChipEstado
import com.example.induztekprotocolos.ui.components.FilaDato
import com.example.induztekprotocolos.ui.components.SelectorDesplegable
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun VisitaDetalleScreen(
    detalle: DetalleVisita?,
    puedeGestionarEquipos: Boolean,
    puedeCerrar: Boolean,
    onVolver: () -> Unit,
    onProtocolo: (idEquipo: Long) -> String?,
    onHistorial: (idEquipo: Long) -> Unit,
    onRegistrarEquipo: (idInstalacion: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?) -> Res<Long>,
    onCerrarVisita: (nombre: String, rut: String, cargo: String) -> Res<Unit>
) {
    var dialogoEquipo by remember { mutableStateOf(false) }
    var dialogoCierre by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (dialogoEquipo && detalle != null) {
        DialogoNuevoEquipo(
            onCancelar = { dialogoEquipo = false },
            onGuardar = { tag, tipo, marca, serie, max ->
                val r = onRegistrarEquipo(detalle.instalacion.id, tag, tipo, marca, serie, max)
                if (r.ok) {
                    dialogoEquipo = false
                    scope.launch { snackbar.showSnackbar("Equipo $tag registrado") }
                }
                r.error
            }
        )
    }
    if (dialogoCierre) {
        DialogoCierreVisita(
            onCancelar = { dialogoCierre = false },
            onCerrar = { nombre, rut, cargo ->
                val r = onCerrarVisita(nombre, rut, cargo)
                if (r.ok) {
                    dialogoCierre = false
                    scope.launch { snackbar.showSnackbar("Visita cerrada") }
                }
                r.error
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = detalle?.visita?.numeroOt ?: "Visita", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (detalle == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(titulo = detalle.instalacion.nombre) {
                FilaDato("Comuna", detalle.instalacion.comuna)
                FilaDato("Fecha", Formato.fecha(detalle.visita.fecha))
                FilaDato("Tipo", detalle.visita.tipoVisita.etiqueta)
                FilaDato("Ingeniero a cargo", detalle.ingeniero)
                if (detalle.instalacion.observacionesAcceso.isNotBlank())
                    Text("Acceso: ${detalle.instalacion.observacionesAcceso}", style = MaterialTheme.typography.bodySmall)
                if (detalle.visita.cerrada) {
                    FilaDato("Cierre", Formato.fechaHora(detalle.visita.fechaHoraCierre ?: 0L))
                    FilaDato("Recibe", "${detalle.visita.receptorNombre} (${detalle.visita.receptorCargo})")
                }
            }

            Text("Equipos de la instalación", style = MaterialTheme.typography.titleMedium)
            detalle.equipos.forEachIndexed { indice, item ->
                AparecerAnimado(indice) {
                    Card(
                        shape = EstilosTarjeta.forma,
                        colors = EstilosTarjeta.colores(),
                        elevation = EstilosTarjeta.elevacion(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.equipo.tag, style = MaterialTheme.typography.titleMedium)
                                val protocolo = item.protocolo
                                if (protocolo != null) ChipEstado(protocolo.estado)
                                else Text("Sin protocolo", style = MaterialTheme.typography.labelMedium)
                            }
                            Text("${item.equipo.tipo.etiqueta} · ${item.equipo.marca}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                if (!detalle.visita.cerrada || item.protocolo != null) {
                                    BotonPrincipal(
                                        texto = if (item.protocolo == null) "Iniciar protocolo" else "Abrir protocolo",
                                        onClick = { onProtocolo(item.equipo.id)?.let { mensaje -> scope.launch { snackbar.showSnackbar(mensaje) } } },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                BotonSecundario(texto = "Historial", onClick = { onHistorial(item.equipo.id) }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (puedeGestionarEquipos && !detalle.visita.cerrada) {
                BotonSecundario(texto = "Registrar equipo nuevo", onClick = { dialogoEquipo = true }, modifier = Modifier.fillMaxWidth())
            }
            if (puedeCerrar && !detalle.visita.cerrada) {
                BotonPrincipal(
                    texto = "Cerrar visita",
                    icono = R.drawable.ic_candado,
                    onClick = { dialogoCierre = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** RF-01 / RN-01: alta de equipo. La función onGuardar devuelve el mensaje de error del negocio (o null si quedó guardado). */
@Composable
private fun DialogoNuevoEquipo(
    onCancelar: () -> Unit,
    onGuardar: (tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?) -> String?
) {
    var tag by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf<TipoEquipo?>(null) }
    var marca by remember { mutableStateOf("") }
    var serie by remember { mutableStateOf("") }
    var maximo by remember { mutableStateOf("") }
    var errorTag by remember { mutableStateOf<String?>(null) }
    var errorTipo by remember { mutableStateOf<String?>(null) }
    var errorMaximo by remember { mutableStateOf<String?>(null) }

    val usaContactos = tipo == TipoEquipo.INTERRUPTOR || tipo == TipoEquipo.CELDA_MT

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Registrar equipo") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                CampoTexto(tag, { tag = it; errorTag = null }, "TAG del equipo", R.drawable.ic_etiqueta, error = errorTag)
                SelectorDesplegable(
                    etiqueta = "Tipo de equipo",
                    opciones = TipoEquipo.entries,
                    seleccionada = tipo,
                    textoDe = { it.etiqueta },
                    onSeleccionar = { tipo = it; errorTipo = null },
                    error = errorTipo
                )
                CampoTexto(marca, { marca = it }, "Marca (opcional)", R.drawable.ic_editar)
                CampoTexto(serie, { serie = it }, "N° de serie (opcional)", R.drawable.ic_editar)
                if (usaContactos) {
                    CampoTexto(maximo, { maximo = it; errorMaximo = null }, "Máx. de placa contactos (µΩ)", R.drawable.ic_editar, error = errorMaximo, tipoTeclado = KeyboardType.Decimal)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errorTag = Validaciones.tag(tag)
                errorTipo = if (tipo == null) "Selecciona el tipo" else null
                val max = if (usaContactos) Validaciones.medicion(maximo) else null
                errorMaximo = max?.error
                if (errorTag == null && errorTipo == null && errorMaximo == null) {
                    val mensaje = onGuardar(tag, tipo!!, marca, serie, max?.valor)
                    if (mensaje != null) errorTag = mensaje
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/** RN-13 / RN-03: cierre con los datos de quien recibe (nombre, RUT válido y cargo). */
@Composable
private fun DialogoCierreVisita(
    onCancelar: () -> Unit,
    onCerrar: (nombre: String, rut: String, cargo: String) -> String?
) {
    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorCargo by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cerrar visita") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                Text("Datos de quien recibe en el cliente", style = MaterialTheme.typography.bodyMedium)
                CampoTexto(nombre, { nombre = it; errorNombre = null }, "Nombre", R.drawable.ic_persona, error = errorNombre)
                CampoTexto(rut, { rut = it; errorRut = null }, "RUT (ej: 12.345.678-5)", R.drawable.ic_etiqueta, error = errorRut)
                CampoTexto(cargo, { cargo = it; errorCargo = null }, "Cargo", R.drawable.ic_editar, error = errorCargo)
                errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errorNombre = Validaciones.nombre(nombre)
                errorRut = Validaciones.rut(rut)
                errorCargo = Validaciones.requerido(cargo, "El cargo")
                if (errorNombre == null && errorRut == null && errorCargo == null) errorGeneral = onCerrar(nombre, rut, cargo)
            }) { Text("Cerrar visita") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
