package com.example.induztekprotocolos.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.domain.PermisosRol
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.ui.components.AparecerAnimado
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.FilaIcono
import com.example.induztekprotocolos.ui.styles.EstilosTarjeta
import com.example.induztekprotocolos.ui.theme.Dimens

private data class OpcionMenu(@DrawableRes val icono: Int, val titulo: String, val subtitulo: String, val accion: () -> Unit)

/** Pantalla de inicio: el menú cambia según el rol (Anexo 7, tabla de permisos). */
@Composable
fun InicioScreen(
    usuario: Usuario,
    pendientesRevision: Int,
    onVisitas: () -> Unit,
    onRevision: () -> Unit,
    onInstrumentos: () -> Unit,
    onMisEquipos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }

    val opciones = buildList {
        if (usuario.rol == Rol.CLIENTE) {
            if (PermisosRol.tiene(usuario.rol, Permiso.CONSULTAR_PROPIA_EMPRESA))
                add(OpcionMenu(R.drawable.ic_historial, "Mis equipos", "Historial de protocolos formalizados de tu empresa", onMisEquipos))
        } else {
            add(OpcionMenu(R.drawable.ic_ubicacion, "Visitas a terreno", "Crear visitas y registrar protocolos", onVisitas))
            if (PermisosRol.tiene(usuario.rol, Permiso.FORMALIZAR_OBSERVAR))
                add(OpcionMenu(R.drawable.ic_check, "Revisión de protocolos", "$pendientesRevision pendiente(s) de formalizar u observar", onRevision))
            add(OpcionMenu(R.drawable.ic_instrumento, "Instrumentos", "Calibración y vencimientos", onInstrumentos))
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de la cuenta de ${usuario.nombre}?") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; onCerrarSesion() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Inicio", acciones = {
                IconButton(onClick = { confirmarSalida = true }) {
                    Icon(painter = painterResource(R.drawable.ic_candado), contentDescription = "Cerrar sesión")
                }
            })
        },
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
            Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.headlineSmall)
            Text(usuario.rol.etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

            opciones.forEachIndexed { indice, opcion ->
                AparecerAnimado(indice) {
                    Card(
                        onClick = opcion.accion,
                        shape = EstilosTarjeta.forma,
                        colors = EstilosTarjeta.colores(),
                        elevation = EstilosTarjeta.elevacion()
                    ) {
                        Row(modifier = Modifier.padding(Dimens.espacioMedio)) {
                            FilaIcono(opcion.icono, opcion.titulo, opcion.subtitulo)
                        }
                    }
                }
            }
        }
    }
}
