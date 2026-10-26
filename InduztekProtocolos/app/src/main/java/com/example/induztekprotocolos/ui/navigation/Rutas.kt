package com.example.induztekprotocolos.ui.navigation

sealed class Rutas(val route: String) {
    object Equipos : Rutas("equipos_screen")
    object ProtocoloForm : Rutas("protocolo_form_screen/{equipoId}") {
        fun crearRuta(equipoId: Long) = "protocolo_form_screen/$equipoId"
    }
    object Historial : Rutas("historial_screen")
}