package com.example.induztekprotocolos.navigation

// Igual que en MiRegistro: cada pantalla es un objeto con su ruta; las que reciben datos arman la ruta con crear().
sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Inicio : Ruta("inicio")
    data object Visitas : Ruta("visitas")
    data object NuevaVisita : Ruta("nueva_visita")
    data object Revision : Ruta("revision")
    data object Instrumentos : Ruta("instrumentos")
    data object MisEquipos : Ruta("mis_equipos")

    data object Visita : Ruta("visita/{id}") {
        fun crear(id: Long) = "visita/$id"
    }

    data object Protocolo : Ruta("protocolo/{id}") {
        fun crear(id: Long) = "protocolo/$id"
    }

    data object Historial : Ruta("historial/{idEquipo}") {
        fun crear(idEquipo: Long) = "historial/$idEquipo"
    }
}
