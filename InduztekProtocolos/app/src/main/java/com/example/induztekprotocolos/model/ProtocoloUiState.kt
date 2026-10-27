package com.example.induztekprotocolos.model

import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoCalculo
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoPrueba

// Igual que FormularioUiState en MiRegistro: un solo objeto inmutable con TODO lo que la pantalla necesita.
// La pantalla solo lo dibuja; el ViewModel lo modifica con .copy() / update { }.

/** Condiciones ambientales del protocolo (RF-03). Los errores son mensajes listos para mostrar. */
data class FormAmbiente(
    val temperatura: String = "",
    val humedad: String = "",
    val descripcion: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null
)

/** Formulario de una prueba (RF-04/05/06). "valores" guarda lo escrito por parámetro (código -> texto). */
data class FormPrueba(
    val visible: Boolean = false,
    val tipo: TipoPrueba? = null,
    val elemento: String = "",
    val momento: MomentoPrueba = MomentoPrueba.UNICA,
    val realizada: Boolean = true,
    val motivo: String = "",
    val idInstrumento: Long? = null,
    val valores: Map<String, String> = emptyMap(),
    val erroresValores: Map<String, String> = emptyMap(),
    val errorTipo: String? = null,
    val errorElemento: String? = null,
    val errorMotivo: String? = null,
    val errorInstrumento: String? = null,
    val errorGeneral: String? = null,
    val estimado: ResultadoCalculo? = null
)

data class HistorialItem(val registro: HistorialEstado, val nombreUsuario: String)

data class ProtocoloUiState(
    val vista: ProtocoloVista? = null,
    val visita: Visita? = null,
    val rol: Rol? = null,
    val puedeEditar: Boolean = false,
    val instrumentos: List<Instrumento> = emptyList(),
    val historial: List<HistorialItem> = emptyList(),
    val ambiente: FormAmbiente = FormAmbiente(),
    val prueba: FormPrueba = FormPrueba(),
    val mensaje: String? = null,
    val errorEstado: String? = null,
    // Sube cada vez que se guarda algo: la pantalla lo usa para disparar la animación de confirmación.
    val guardados: Int = 0
)
