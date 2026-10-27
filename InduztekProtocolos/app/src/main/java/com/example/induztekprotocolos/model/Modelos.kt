package com.example.induztekprotocolos.model

import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita

// Modelos planos (como Usuario en MiRegistro): sin anotaciones, los llena UsuarioDbHelper -> InduztekDbHelper.

/** Personal de Induztek O cuenta de una empresa cliente (rol CLIENTE; "nombre" es la razón social). */
data class Usuario(
    val id: Long = 0,
    val rol: Rol,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String = "",
    val activo: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null
)

data class Instalacion(
    val id: Long = 0,
    val idUsuarioCliente: Long?,
    val nombre: String,
    val comuna: String,
    val observacionesAcceso: String = ""
)

/** RN-01: el TAG es único DENTRO de una instalación. */
data class Equipo(
    val id: Long = 0,
    val idInstalacion: Long,
    val tag: String,
    val tipo: TipoEquipo,
    val marca: String = "",
    val numeroSerie: String = "",
    val tensiones: String = "",
    val maxContactosUohm: Double? = null
)

data class Instrumento(
    val id: Long = 0,
    val nombre: String,
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val fechaUltimaCalibracion: Long,
    val fechaVencimiento: Long
)

data class Visita(
    val id: Long = 0,
    val numeroOt: String,
    val idInstalacion: Long,
    val fecha: Long,
    val tipoVisita: TipoVisita,
    val idIngenieroCargo: Long,
    val fechaHoraCierre: Long? = null,
    val receptorNombre: String? = null,
    val receptorRut: String? = null,
    val receptorCargo: String? = null
) {
    val cerrada get() = fechaHoraCierre != null
}

/** Un protocolo por equipo y visita. El resultado global se calcula desde sus pruebas (RN-10), no se guarda. */
data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuarioRegistra: Long,
    val fechaHoraPrueba: Long,
    val temperaturaC: Double? = null,
    val humedadPct: Double? = null,
    val descripcionMantenimiento: String? = null,
    val estado: EstadoProtocolo = EstadoProtocolo.BORRADOR,
    // Recurso nativo (Cámara/Galería): la foto como BYTES, igual que el avatar de MiRegistro (columna BLOB).
    val foto: ByteArray? = null,
    // Recurso nativo (Ubicación)
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class Prueba(
    val id: Long = 0,
    val idProtocolo: Long,
    val tipoPrueba: TipoPrueba,
    val elemento: String,
    val momento: MomentoPrueba = MomentoPrueba.UNICA,
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null,
    val idInstrumento: Long? = null,
    val resultado: ResultadoPrueba? = null,
    val valorDerivado: Double? = null,
    val etiquetaDerivada: String? = null
)

data class Medicion(val id: Long = 0, val idPrueba: Long, val parametro: String, val valor: Double, val unidad: String)

/** RNF-07: quién cambió el estado, cuándo y por qué. */
data class HistorialEstado(
    val id: Long = 0,
    val idProtocolo: Long,
    val estadoAnterior: EstadoProtocolo?,
    val estadoNuevo: EstadoProtocolo,
    val idUsuario: Long,
    val fechaHora: Long,
    val motivo: String? = null
)
