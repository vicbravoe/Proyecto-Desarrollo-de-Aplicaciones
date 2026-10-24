package cl.induztek.protocolos.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.protocolos.data.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquiposViewModel(private val repository: ProtocoloRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EquiposUiState())
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observarEquipos().collect { equipos ->
                _uiState.update { it.copy(cargando = false, equipos = equipos) }
            }
        }
    }
}