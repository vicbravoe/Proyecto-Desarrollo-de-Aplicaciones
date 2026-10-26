package com.example.induztekprotocolos.ui.protocolo

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.induztekprotocolos.data.local.ResultadoPrueba
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.BotonSecundario
import com.example.induztekprotocolos.ui.components.CampoSeleccion
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.theme.Dimens
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun ProtocoloFormScreen(
    viewModel: ProtocoloViewModel,
    onProtocoloGuardado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- recurso nativo 1: cámara ---
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val salida = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, salida)
            viewModel.onFotoCapturada(salida.toByteArray())
        }
    }
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzadorCamara.launch(null)
    }

    // --- recurso nativo 2: ubicación ---
    val executor = remember { Executors.newSingleThreadExecutor() }
    fun obtenerUbicacion() {
        val locationManager = context.getSystemService(LocationManager::class.java)
        val proveedor = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (proveedor == null) {
            viewModel.onUbicacionFallida()
            return
        }
        viewModel.onComenzarAObtenerUbicacion()
        LocationManagerCompat.getCurrentLocation(
            locationManager, proveedor, CancellationSignal(), executor
        ) { ubicacion ->
            if (ubicacion != null) {
                viewModel.onUbicacionObtenida(ubicacion.latitude, ubicacion.longitude)
            } else {
                viewModel.onUbicacionFallida()
            }
        }
    }
    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) obtenerUbicacion() else viewModel.onUbicacionFallida()
    }

    // Al guardar con éxito, dejamos ver la animación de confirmación un instante
    // antes de volver a la lista — si navegáramos de inmediato, nunca se vería.
    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            kotlinx.coroutines.delay(900)
            onProtocoloGuardado()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.espacioMedio),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
    ) {
        item {
            uiState.equipo?.let { equipo ->
                Text("${equipo.codigo} · ${equipo.tipo.etiqueta}", style = MaterialTheme.typography.titleMedium)
                Text(equipo.instalacion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            CampoTexto(
                valor = uiState.valorMedicion,
                onValorCambia = viewModel::onValorCambia,
                etiqueta = "Valor medido",
                error = uiState.errorValor,
                tipoTeclado = KeyboardType.Decimal
            )
        }
        item {
            CampoTexto(
                valor = uiState.unidadMedicion,
                onValorCambia = viewModel::onUnidadCambia,
                etiqueta = "Unidad (Ω, kV, A...)",
                error = uiState.errorUnidad
            )
        }
        item {
            CampoSeleccion(
                opciones = ResultadoPrueba.entries,
                seleccionada = uiState.resultado,
                onSeleccionar = viewModel::onResultadoSeleccionado,
                etiqueta = "Resultado de la prueba",
                textoDe = { it.etiqueta },
                error = uiState.errorResultado
            )
        }
        item {
            CampoTexto(
                valor = uiState.observaciones,
                onValorCambia = viewModel::onObservacionesCambia,
                etiqueta = "Observaciones (opcional)"
            )
        }

        // --- bloque del recurso nativo 1: cámara ---
        item {
            Column {
                BotonSecundario(
                    texto = if (uiState.fotoEvidencia == null) "Tomar foto de evidencia" else "Volver a tomar foto",
                    onClick = {
                        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                        if (concedido) lanzadorCamara.launch(null) else lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = uiState.fotoEvidencia != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    uiState.fotoEvidencia?.let { bytes ->
                        val bitmap = remember(bytes) {
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Foto de evidencia de la prueba",
                            modifier = Modifier
                                .padding(top = Dimens.espacioChico)
                                .size(140.dp)
                        )
                    }
                }
            }
        }

        // --- bloque del recurso nativo 2: ubicación ---
        item {
            Column {
                BotonSecundario(
                    texto = if (uiState.latitud == null) "Capturar ubicación" else "Actualizar ubicación",
                    onClick = {
                        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED
                        if (concedido) obtenerUbicacion() else lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    habilitado = !uiState.obteniendoUbicacion,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(visible = uiState.obteniendoUbicacion, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Dimens.espacioChico)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(" Obteniendo ubicación...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = Dimens.espacioChico))
                    }
                }
                AnimatedVisibility(visible = uiState.latitud != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Dimens.espacioChico)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
                        Text(
                            "  %.5f, %.5f".format(uiState.latitud, uiState.longitud),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            BotonPrincipal(
                texto = "Guardar protocolo",
                onClick = viewModel::onGuardar,
                cargando = uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            AnimatedVisibility(visible = uiState.guardadoExitoso, enter = fadeIn() + expandVertically()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "  Protocolo guardado",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = Dimens.espacioChico)
                    )
                }
            }
        }
    }
}
