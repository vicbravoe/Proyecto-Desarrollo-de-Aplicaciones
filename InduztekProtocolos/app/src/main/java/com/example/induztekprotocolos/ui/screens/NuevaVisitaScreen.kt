package com.example.induztekprotocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Res
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.CasillaConTexto
import com.example.induztekprotocolos.ui.components.GrupoRadio
import com.example.induztekprotocolos.ui.components.SelectorDesplegable
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.theme.Dimens

/** HU: iniciar una visita a terreno (RF-02). Número de OT, fecha y estado se generan solos. */
@Composable
fun NuevaVisitaScreen(
    usuario: Usuario,
    instalaciones: List<Instalacion>,
    ingenieros: List<Usuario>,
    tecnicos: List<Usuario>,
    onVolver: () -> Unit,
    onCrear: (idInstalacion: Long, tipo: TipoVisita, idIngeniero: Long, participantes: List<Long>) -> Res<Long>,
    onCreada: (Long) -> Unit
) {
    var instalacion by remember { mutableStateOf<Instalacion?>(null) }
    var tipo by remember { mutableStateOf(TipoVisita.SOLO_PRUEBAS) }
    var ingeniero by remember { mutableStateOf(ingenieros.firstOrNull { it.id == usuario.id }) }
    var participantes by remember { mutableStateOf(if (usuario.rol == Rol.TECNICO) setOf(usuario.id) else emptySet()) }
    var errorInstalacion by remember { mutableStateOf<String?>(null) }
    var errorIngeniero by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Nueva visita", onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(titulo = "Instalación") {
                SelectorDesplegable(
                    etiqueta = "Instalación del cliente",
                    opciones = instalaciones,
                    seleccionada = instalacion,
                    textoDe = { "${it.nombre} (${it.comuna})" },
                    onSeleccionar = { instalacion = it; errorInstalacion = null },
                    error = errorInstalacion
                )
            }
            TarjetaSeccion(titulo = "Tipo de visita") {
                GrupoRadio(
                    opciones = TipoVisita.entries,
                    seleccionada = tipo,
                    onSeleccionar = { tipo = it },
                    textoDe = { it.etiqueta }
                )
            }
            TarjetaSeccion(titulo = "Equipo de trabajo") {
                SelectorDesplegable(
                    etiqueta = "Ingeniero a cargo",
                    opciones = ingenieros,
                    seleccionada = ingeniero,
                    textoDe = { it.nombre },
                    onSeleccionar = { ingeniero = it; errorIngeniero = null },
                    error = errorIngeniero
                )
                Text("Técnicos participantes", style = MaterialTheme.typography.bodyMedium)
                tecnicos.forEach { tecnico ->
                    CasillaConTexto(
                        texto = tecnico.nombre,
                        marcado = tecnico.id in participantes,
                        onCambio = { marcado -> participantes = if (marcado) participantes + tecnico.id else participantes - tecnico.id }
                    )
                }
            }
            errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            BotonPrincipal(
                texto = "Crear visita",
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    errorInstalacion = if (instalacion == null) "Selecciona la instalación" else null
                    errorIngeniero = if (ingeniero == null) "Selecciona el ingeniero a cargo" else null
                    val inst = instalacion
                    val ing = ingeniero
                    if (inst != null && ing != null) {
                        val resultado = onCrear(inst.id, tipo, ing.id, participantes.toList())
                        if (resultado.ok && resultado.valor != null) onCreada(resultado.valor) else errorGeneral = resultado.error
                    }
                }
            )
        }
    }
}
