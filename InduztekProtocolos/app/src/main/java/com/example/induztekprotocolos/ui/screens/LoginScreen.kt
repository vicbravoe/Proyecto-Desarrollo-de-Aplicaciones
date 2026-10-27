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
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.ui.components.BarraSuperior
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.SelectorDesplegable
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.ui.theme.InduztekTheme

/**
 * Inicio de sesión SIMULADO: el caso no pide autenticación real, así que se elige una de las cuentas
 * ficticias (técnico, ingeniero, jefe o cliente) para probar los permisos de cada rol (Anexo 7).
 */
@Composable
fun LoginScreen(cuentas: List<Usuario>, onEntrar: (Long) -> Unit) {
    var elegida by remember { mutableStateOf<Usuario?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Induztek Protocolos") },
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
            Text("Formalización de protocolos de prueba en terreno", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Datos 100 % ficticios. Elige una cuenta para entrar; cada rol ve y puede hacer cosas distintas.",
                style = MaterialTheme.typography.bodyMedium
            )
            TarjetaSeccion(titulo = "Ingresar") {
                SelectorDesplegable(
                    etiqueta = "Cuenta",
                    opciones = cuentas,
                    seleccionada = elegida,
                    textoDe = { "${it.nombre} · ${it.rol.etiqueta}" },
                    onSeleccionar = { elegida = it; error = null },
                    error = error
                )
                BotonPrincipal(
                    texto = "Entrar",
                    icono = R.drawable.ic_persona,
                    onClick = {
                        val cuenta = elegida
                        if (cuenta == null) error = "Selecciona una cuenta" else onEntrar(cuenta.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    InduztekTheme {
        LoginScreen(
            cuentas = listOf(Usuario(1, Rol.TECNICO, "12.345.678-5", "Ana Pérez", "ana@induztek.cl")),
            onEntrar = {}
        )
    }
}
