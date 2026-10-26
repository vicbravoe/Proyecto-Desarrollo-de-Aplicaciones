package com.example.induztekprotocolos.ui.protocolo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztekprotocolos.data.local.ProtocoloEntity
import com.example.induztekprotocolos.data.repository.ProtocoloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtocoloViewModel(
    private val repository: ProtocoloRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtocoloUiState())
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    fun cargarEquipo(equipoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            val equipo = repository.getEquipoById(equipoId)
            _uiState.update { it.copy(equipo = equipo, estaCargando = false) }
        }
    }

    fun onTecnicoChanged(val: String) { _uiState.update { it.copy(tecnicoNombre = val) } }
    fun onVoltajeChanged(val: String) { _uiState.update { it.copy(voltajeMedido = val) } }
    fun onCorrienteChanged(val: String) { _uiState.update { it.copy(corrienteMedida = val) } }
    fun onResultadoChanged(val: String) { _uiState.update { it.copy(resultado = val) } }
    fun onObservacionesChanged(val: String) { _uiState.update { it.copy(observaciones = val) } }

    fun guardarProtocolo() {
        val state = _uiState.value

        if (state.tecnicoNombre.isBlank() || state.voltajeMedido.isBlank() || state.corrienteMedida.isBlank()) {
            _uiState.update {
                it.copy(
                    esValido = false,
                    mensajeError = "Por favor completa todos los campos obligatorios."
                )
            }
            return
        }

        val equipo = state.equipo ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }

            val nuevoProtocolo = ProtocoloEntity(
                equipoId = equipo.id,
                nombreEquipo = equipo.nombre,
                codigoEquipo = equipo.codigo,
                tecnicoNombre = state.tecnicoNombre.trim(),
                fechaTimestamp = System.currentTimeMillis(),
                voltajeMedido = state.voltajeMedido.toDoubleOrNull() ?: 0.0,
                corrienteMedida = state.corrienteMedida.toDoubleOrNull() ?: 0.0,
                resultado = state.resultado,
                observaciones = state.observaciones.trim()
            )

            repository.insertProtocolo(nuevoProtocolo)
            _uiState.update { it.copy(estaCargando = false, guardadoExitoso = true) }
        }
    }
}