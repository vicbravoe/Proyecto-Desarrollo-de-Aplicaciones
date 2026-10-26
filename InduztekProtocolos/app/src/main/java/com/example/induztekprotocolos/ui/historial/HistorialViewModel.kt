package cl.induztek.protocolos.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.protocolos.data.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistorialViewModel(
    equipoId: Long,
    private val repository: ProtocoloRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val equipo = repository.obtenerEquipo(equipoId)
            _uiState.update { it.copy(equipo = equipo) }
        }
        viewModelScope.launch {
            repository.observarHistorialDe(equipoId).collect { protocolos ->
                _uiState.update { it.copy(protocolos = protocolos) }
            }
        }
    }
}