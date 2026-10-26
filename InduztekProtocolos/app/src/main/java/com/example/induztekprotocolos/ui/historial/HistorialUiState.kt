package cl.induztek.protocolos.ui.historial

import cl.induztek.protocolos.data.local.EquipoEntity
import cl.induztek.protocolos.data.local.ProtocoloEntity

data class HistorialUiState(
    val equipo: EquipoEntity? = null,
    val protocolos: List<ProtocoloEntity> = emptyList()
)