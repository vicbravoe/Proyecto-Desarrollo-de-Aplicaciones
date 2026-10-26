package com.example.induztekprotocolos.ui.equipos

import com.example.induztekprotocolos.data.local.EquipoEntity

data class EquiposUiState(
    val cargando: Boolean = true,
    val equipos: List<EquipoEntity> = emptyList()
)
