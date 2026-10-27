package com.example.induztekprotocolos.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.data.ConvertidorImagen
import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TransicionesEstado
import com.example.induztekprotocolos.model.ProtocoloUiState
import com.example.induztekprotocolos.model.PruebaVista
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.permisos.Permisos
import com.example.induztekprotocolos.permisos.Ubicacion
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.BotonSecundario
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.components.ChipEstado
import com.example.induztekprotocolos.ui.components.ChipResultado
import com.example.induztekprotocolos.ui.components.Etiqueta
import com.example.induztekprotocolos.ui.components.FilaDato
import com.example.induztekprotocolos.ui.components.GrupoRadio
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.Exito
import com.example.induztekprotocolos.viewmodel.ProtocoloViewModel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProtocoloScreen(
    idProtocolo: Long,
    usuario: Usuario,
    onVolver: () -> Unit,
    onHistorial: (idEquipo: Long) -> Unit,
    viewModel: ProtocoloViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(idProtocolo) { viewModel.cargar(idProtocolo, usuario) }

    // Avisos del ViewModel (guardado, errores) en un Snackbar.
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    // ANIMACIÓN de confirmación: al guardar algo aparece un "✓ Guardado" que luego se desvanece.
    var mostrarOk by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.guardados) {
        if (uiState.guardados > 0) {
            mostrarOk = true
            delay(1600)
            mostrarOk = false
        }
    }

    // ----- Permisos de Android (mismo flujo de 4 pasos de clases) -----
    val textoCamara = "Sin permiso de cámara no puedo tomar la foto de evidencia."
    val textoGaleria = "Sin permiso de imágenes no puedo abrir la galería."
    val textoUbicacion = "Sin permiso de ubicación no puedo registrar dónde se hizo la prueba."
    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val r = snackbar.showSnackbar(message = mensaje, actionLabel = "Ajustes", duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    // RECURSO NATIVO 1 · CÁMARA
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto: Bitmap? ->
        if (foto != null) viewModel.onFotoCamara(foto)
    }
    fun abrirCamara() {
        try { lanzadorCamara.launch(null) } catch (e: ActivityNotFoundException) { scope.launch { snackbar.showSnackbar("Este equipo no tiene una app de cámara") } }
    }
    val pedirCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamara)
    }

    // RECURSO NATIVO 2 · GALERÍA
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) viewModel.onFotoGaleria(uri)
    }
    fun abrirGaleria() {
        try { lanzadorGaleria.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)) } catch (e: ActivityNotFoundException) { }
    }
    val pedirImagenes = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado(textoGaleria)
    }

    // RECURSO NATIVO 3 · UBICACIÓN (GPS)
    fun leerUbicacion() {
        Ubicacion.obtener(context) { ubicacion ->
            if (ubicacion != null) viewModel.onUbicacion(ubicacion.latitude, ubicacion.longitude) else viewModel.onUbicacionNoDisponible()
        }
    }
    val pedirUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (concedido) leerUbicacion() else avisarPermisoDenegado(textoUbicacion)
    }

    // Diálogo del motivo (obligatorio al observar o anular, RN-04)
    var estadoConMotivo by remember { mutableStateOf<EstadoProtocolo?>(null) }
    estadoConMotivo?.let { destino ->
        DialogoMotivo(
            destino = destino,
            onCancelar = { estadoConMotivo = null },
            onConfirmar = { motivo ->
                viewModel.cambiarEstado(destino, motivo)
                estadoConMotivo = null
            }
        )
    }

    val vista = uiState.vista
    Scaffold(
        topBar = { BarraSuperior(titulo = vista?.protocolo?.numero ?: "Protocolo", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (vista == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            // ---------- Cabecera ----------
            TarjetaSeccion(titulo = "${vista.equipo.tag} · ${vista.equipo.tipo.etiqueta}") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ChipEstado(vista.protocolo.estado)
                    AnimatedVisibility(visible = mostrarOk, enter = scaleIn() + fadeIn(), exit = fadeOut()) {
                        Surface(color = Exito, shape = RoundedCornerShape(50)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("  Guardado", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                FilaDato("Fecha de la prueba", Formato.fechaHora(vista.protocolo.fechaHoraPrueba))
                FilaDato("Marca / serie", "${vista.equipo.marca} / ${vista.equipo.numeroSerie}")
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico), verticalAlignment = Alignment.CenterVertically) {
                    Text("Resultado global:", style = MaterialTheme.typography.bodyMedium)
                    ChipResultado(vista.global)
                }
                BotonSecundario(texto = "Ver historial del equipo", onClick = { onHistorial(vista.equipo.id) }, modifier = Modifier.fillMaxWidth())
            }

            // ---------- Condiciones ambientales ----------
            TarjetaSeccion(titulo = "Condiciones ambientales") {
                if (uiState.puedeEditar) {
                    CampoTexto(uiState.ambiente.temperatura, viewModel::onTemperatura, "Temperatura (°C)", R.drawable.ic_termometro, error = uiState.ambiente.errorTemperatura, tipoTeclado = KeyboardType.Decimal)
                    CampoTexto(uiState.ambiente.humedad, viewModel::onHumedad, "Humedad relativa (%)", R.drawable.ic_gota, error = uiState.ambiente.errorHumedad, tipoTeclado = KeyboardType.Decimal)
                    CampoTexto(uiState.ambiente.descripcion, viewModel::onDescripcion, "Descripción del mantenimiento (opcional)", R.drawable.ic_editar)
                    BotonPrincipal(texto = "Guardar condiciones", icono = R.drawable.ic_check, onClick = viewModel::guardarAmbiente, modifier = Modifier.fillMaxWidth())
                } else {
                    FilaDato("Temperatura", vista.protocolo.temperaturaC?.let { "${Formato.numero(it)} °C" } ?: "—")
                    FilaDato("Humedad", vista.protocolo.humedadPct?.let { "${Formato.numero(it)} %" } ?: "—")
                    vista.protocolo.descripcionMantenimiento?.let { FilaDato("Mantenimiento", it) }
                }
            }

            // ---------- Evidencia: cámara, galería y ubicación ----------
            TarjetaSeccion(titulo = "Evidencia") {
                val foto = remember(vista.protocolo.foto) { vista.protocolo.foto?.let { ConvertidorImagen.aBitmap(it)?.asImageBitmap() } }
                if (foto != null) {
                    Image(
                        bitmap = foto,
                        contentDescription = "Foto de evidencia del equipo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(Dimens.radioCampo))
                    )
                } else {
                    Text("Sin foto todavía (idealmente de la placa del equipo).", style = MaterialTheme.typography.bodySmall)
                }
                val lat = vista.protocolo.latitud
                val lon = vista.protocolo.longitud
                Text(
                    if (lat != null && lon != null) "Ubicación: ${Formato.coordenada(lat)}, ${Formato.coordenada(lon)}" else "Ubicación sin registrar",
                    style = MaterialTheme.typography.bodySmall
                )
                if (uiState.puedeEditar) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        BotonSecundario(texto = "Cámara", modifier = Modifier.weight(1f), onClick = {
                            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara() else pedirCamara.launch(Permisos.CAMARA)
                        })
                        BotonSecundario(texto = "Galería", modifier = Modifier.weight(1f), onClick = {
                            if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria() else pedirImagenes.launch(Permisos.IMAGENES)
                        })
                    }
                    BotonSecundario(texto = "Registrar ubicación (GPS)", modifier = Modifier.fillMaxWidth(), onClick = {
                        if (Permisos.tiene(context, Permisos.UBICACION)) leerUbicacion() else pedirUbicacion.launch(Permisos.UBICACION)
                    })
                }
            }

            // ---------- Pruebas ----------
            TarjetaSeccion(titulo = "Pruebas (${vista.pruebas.size})") {
                if (vista.pruebas.isEmpty()) Text("Aún no hay pruebas registradas.", style = MaterialTheme.typography.bodyMedium)
                vista.pruebas.forEachIndexed { indice, p ->
                    AparecerAnimado(indice) {
                        TarjetaPrueba(
                            p = p,
                            etiquetas = CatalogoPruebas.parametrosDe(p.prueba.tipoPrueba, vista.equipo.tipo).associate { it.codigo to it.etiqueta },
                            puedeDefinirResultado = uiState.rol in listOf(Rol.INGENIERO, Rol.JEFE_TECNICO) &&
                                vista.protocolo.estado == EstadoProtocolo.REGISTRADO_TERRENO &&
                                p.prueba.realizada && CatalogoPruebas.esManual(p.prueba.tipoPrueba),
                            onResultado = { viewModel.fijarResultado(p.prueba.id, it) }
                        )
                    }
                }
                if (uiState.puedeEditar && !uiState.prueba.visible) {
                    BotonPrincipal(texto = "Agregar prueba", icono = R.drawable.ic_agregar, onClick = viewModel::abrirFormPrueba, modifier = Modifier.fillMaxWidth())
                }
            }
            FormularioPrueba(uiState.prueba, vista.equipo, uiState.visita, uiState.instrumentos, viewModel)

            // ---------- Cambio de estado (RN-04) ----------
            AccionesEstado(uiState = uiState, onCambiar = { destino ->
                if (TransicionesEstado.requiereMotivo(destino)) estadoConMotivo = destino else viewModel.cambiarEstado(destino, null)
            })

            // ---------- Historial de estados (RNF-07) ----------
            TarjetaSeccion(titulo = "Historial de estados") {
                if (uiState.historial.isEmpty()) Text("Sin cambios de estado todavía.", style = MaterialTheme.typography.bodySmall)
                uiState.historial.forEach { h ->
                    Column {
                        Text(
                            "${h.registro.estadoAnterior?.etiqueta ?: "Creado"} → ${h.registro.estadoNuevo.etiqueta}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("${Formato.fechaHora(h.registro.fechaHora)} · ${h.nombreUsuario}", style = MaterialTheme.typography.bodySmall)
                        h.registro.motivo?.let { Text("Motivo: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPrueba(
    p: PruebaVista,
    etiquetas: Map<String, String>,
    puedeDefinirResultado: Boolean,
    onResultado: (ResultadoPrueba) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(Dimens.radioCampo),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioChico), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${p.prueba.tipoPrueba.etiqueta} · ${p.prueba.elemento}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (p.prueba.realizada) ChipResultado(p.prueba.resultado) else Etiqueta("No realizada", Color(0xFF7A7F87))
            }
            if (p.prueba.momento != MomentoPrueba.UNICA) Text(p.prueba.momento.etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (!p.prueba.realizada) {
                Text("Motivo: ${p.prueba.motivoNoRealizada}", style = MaterialTheme.typography.bodySmall)
            } else {
                p.mediciones.forEach { m ->
                    Text("${etiquetas[m.parametro] ?: m.parametro}: ${Formato.numero(m.valor)} ${m.unidad}", style = MaterialTheme.typography.bodySmall)
                }
                p.prueba.valorDerivado?.let { Text("${p.prueba.etiquetaDerivada ?: "Valor"}: ${Formato.numero(it)}", style = MaterialTheme.typography.bodySmall) }
                p.instrumento?.let { Text("Instrumento: $it", style = MaterialTheme.typography.bodySmall) }
                // RN-09: comparación con la prueba anterior del mismo equipo y elemento
                if (p.valorActual != null && p.valorAnterior != null) {
                    Text(
                        "Anterior ${Formato.numero(p.valorAnterior)} → actual ${Formato.numero(p.valorActual)}" +
                            (p.variacionPct?.let { " (${Formato.porcentaje(it)})" } ?: ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (p.significativa) Etiqueta("Variación significativa (>25 %)", Color(0xFFC77D12))
                }
                if (puedeDefinirResultado) {
                    Text("Define el resultado (RN-08c):", style = MaterialTheme.typography.bodySmall)
                    GrupoRadio<ResultadoPrueba?>(
                        opciones = ResultadoPrueba.entries,
                        seleccionada = p.prueba.resultado,
                        onSeleccionar = { r -> if (r != null) onResultado(r) },
                        textoDe = { it?.etiqueta ?: "" }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccionesEstado(uiState: ProtocoloUiState, onCambiar: (EstadoProtocolo) -> Unit) {
    val vista = uiState.vista ?: return
    val rol = uiState.rol ?: return
    val estado = vista.protocolo.estado
    val destinos = listOf(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, EstadoProtocolo.OBSERVADO, EstadoProtocolo.ANULADO)
        .filter { TransicionesEstado.permitida(estado, it, rol) }
        .filter { it != EstadoProtocolo.REGISTRADO_TERRENO || uiState.puedeEditar }

    TarjetaSeccion(titulo = "Estado: ${estado.etiqueta}") {
        if (TransicionesEstado.esFinal(estado)) {
            Text("Protocolo formalizado: ya no se puede modificar ni anular.", style = MaterialTheme.typography.bodyMedium)
        } else if (destinos.isEmpty()) {
            Text("No tienes acciones disponibles para este protocolo en este estado.", style = MaterialTheme.typography.bodyMedium)
        }
        uiState.errorEstado?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        destinos.forEach { destino ->
            val texto = when (destino) {
                EstadoProtocolo.REGISTRADO_TERRENO -> "Registrar en terreno"
                EstadoProtocolo.FORMALIZADO -> "Formalizar"
                EstadoProtocolo.OBSERVADO -> "Observar"
                EstadoProtocolo.ANULADO -> "Anular"
                EstadoProtocolo.BORRADOR -> "Borrador"
            }
            if (destino == EstadoProtocolo.FORMALIZADO || destino == EstadoProtocolo.REGISTRADO_TERRENO)
                BotonPrincipal(texto = texto, onClick = { onCambiar(destino) }, modifier = Modifier.fillMaxWidth())
            else
                BotonSecundario(texto = texto, onClick = { onCambiar(destino) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DialogoMotivo(destino: EstadoProtocolo, onCancelar: () -> Unit, onConfirmar: (String) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(destino.etiqueta) },
        text = {
            CampoTexto(motivo, { motivo = it; error = null }, "Motivo (obligatorio)", R.drawable.ic_editar, error = error)
        },
        confirmButton = {
            TextButton(onClick = {
                if (motivo.isBlank()) error = "El motivo es obligatorio" else onConfirmar(motivo)
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
