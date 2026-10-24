package cl.induztek.protocolos.ui.equipos

import cl.induztek.protocolos.data.local.EquipoEntity

data class EquiposUiState(
    val cargando: Boolean = true,
    val equipos: List<EquipoEntity> = emptyList()
)
