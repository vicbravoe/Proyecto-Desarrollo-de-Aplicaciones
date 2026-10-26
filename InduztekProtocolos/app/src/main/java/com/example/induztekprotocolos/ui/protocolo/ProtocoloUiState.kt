package com.example.induztekprotocolos.ui.protocolo

import com.example.induztekprotocolos.data.local.EquipoEntity

data class ProtocoloUiState(
    val equipo: EquipoEntity? = null,
    val tecnicoNombre: String = "",
    val voltajeMedido: String = "",
    val corrienteMedida: String = "",
    val resultado: String = "Aprobado",
    val observaciones: String = "",
    val esValido: Boolean = true,
    val mensajeError: String? = null,
    val guardadoExitoso: Boolean = false,
    val estaCargando: Boolean = false
)