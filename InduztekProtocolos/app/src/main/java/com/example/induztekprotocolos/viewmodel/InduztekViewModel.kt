package com.example.induztekprotocolos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induztekprotocolos.data.InduztekDbHelper
import com.example.induztekprotocolos.data.InduztekRepositorio
import com.example.induztekprotocolos.data.SesionPreferencias
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.domain.PermisosRol
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.model.DetalleVisita
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.EquipoEnVisita
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.model.Res
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.VisitaResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel "de la app" (como UsuariosViewModel en MiRegistro): lo comparten todas las pantallas
 * desde AppNavegacion. Guarda la sesión, las listas y las acciones; el SQL lo hace InduztekDbHelper
 * y las reglas de negocio (RN-xx) las aplica InduztekRepositorio.
 */
class InduztekViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper.obtener(application)
    private val repo = InduztekRepositorio(db)
    private val sesion = SesionPreferencias(application)

    /** Inicio de sesión simulado (el caso no pide autenticación real): se elige una cuenta ficticia. */
    val cuentas: List<Usuario> = db.listarUsuariosActivos()

    private val _usuarioActual = MutableStateFlow(db.buscarUsuario(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _visitas = MutableStateFlow<List<VisitaResumen>>(emptyList())
    val visitas: StateFlow<List<VisitaResumen>> = _visitas.asStateFlow()

    private val _instrumentos = MutableStateFlow<List<Instrumento>>(emptyList())
    val instrumentos: StateFlow<List<Instrumento>> = _instrumentos.asStateFlow()

    private val _porRevisar = MutableStateFlow<List<ProtocoloVista>>(emptyList())
    val porRevisar: StateFlow<List<ProtocoloVista>> = _porRevisar.asStateFlow()

    private val _detalle = MutableStateFlow<DetalleVisita?>(null)
    val detalle: StateFlow<DetalleVisita?> = _detalle.asStateFlow()
    private var idDetalle = 0L

    init { recargar() }

    // ------------------------------- Sesión y permisos -------------------------------
    fun iniciarSesion(id: Long) {
        _usuarioActual.value = db.buscarUsuario(id) ?: return
        sesion.guardar(id)
        recargar()
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    /** Anexo 7: ¿el rol del usuario actual tiene este permiso? */
    fun puede(permiso: Permiso): Boolean = _usuarioActual.value?.let { PermisosRol.tiene(it.rol, permiso) } == true

    // ------------------------------- Carga de listas -------------------------------
    fun recargar() {
        _visitas.value = db.listarVisitas().map { v ->
            val protocolos = db.listarProtocolosDeVisita(v.id)
            VisitaResumen(v, db.buscarInstalacion(v.idInstalacion)?.nombre ?: "—", protocolos.size, protocolos.count { it.estado == EstadoProtocolo.FORMALIZADO })
        }
        _instrumentos.value = db.listarInstrumentos()
        _porRevisar.value = db.listarProtocolosPorEstado(EstadoProtocolo.REGISTRADO_TERRENO).mapNotNull { repo.armarVista(it) }
        if (idDetalle != 0L) cargarDetalle(idDetalle)
    }

    fun fechaVencimientoOk(i: Instrumento): Boolean = i.fechaVencimiento >= System.currentTimeMillis()

    // ------------------------------- Catálogos para formularios -------------------------------
    fun instalaciones(): List<Instalacion> = db.listarInstalaciones()
    fun ingenieros(): List<Usuario> = db.listarPersonalPorRol(Rol.INGENIERO)
    fun tecnicos(): List<Usuario> = db.listarPersonalPorRol(Rol.TECNICO)
    fun equiposDe(idInstalacion: Long): List<Equipo> = db.listarEquiposDeInstalacion(idInstalacion)
    fun instalacionesDelCliente(): List<Instalacion> =
        _usuarioActual.value?.let { db.listarInstalacionesDeCliente(it.id) } ?: emptyList()

    // ------------------------------- Visitas -------------------------------
    private fun <T> intentar(bloque: () -> T): Res<T> =
        try { Res(bloque()) } catch (e: ErrorNegocio) { Res(error = e.message) }

    fun crearVisita(idInstalacion: Long, tipo: TipoVisita, idIngeniero: Long, participantes: List<Long>): Res<Long> =
        intentar { repo.iniciarVisita(idInstalacion, tipo, idIngeniero, participantes) }.also { recargar() }

    fun cargarDetalle(idVisita: Long) {
        idDetalle = idVisita
        val visita = db.buscarVisita(idVisita) ?: return
        val instalacion = db.buscarInstalacion(visita.idInstalacion) ?: return
        _detalle.value = DetalleVisita(
            visita = visita,
            instalacion = instalacion,
            equipos = db.listarEquiposDeInstalacion(instalacion.id).map { EquipoEnVisita(it, db.buscarProtocoloDeVisitaYEquipo(idVisita, it.id)) },
            ingeniero = db.buscarUsuario(visita.idIngenieroCargo)?.nombre ?: "—"
        )
    }

    fun cerrarVisita(idVisita: Long, nombre: String, rut: String, cargo: String): Res<Unit> =
        intentar { repo.cerrarVisita(idVisita, nombre, rut, cargo) }.also { recargar() }

    // ------------------------------- Equipos e instrumentos -------------------------------
    fun registrarEquipo(idInstalacion: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?): Res<Long> =
        intentar { repo.registrarEquipo(idInstalacion, tag, tipo, marca, serie, maxContactos) }.also { recargar() }

    fun registrarInstrumento(nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long): Res<Long> =
        intentar { repo.registrarInstrumento(nombre, marca, modelo, serie, calibracion, vencimiento) }.also { recargar() }

    // ------------------------------- Protocolos -------------------------------
    /** Abre (o crea) el protocolo del equipo dentro de la visita. Devuelve su id. */
    fun abrirProtocolo(idVisita: Long, idEquipo: Long): Res<Long> {
        val u = _usuarioActual.value ?: return Res(error = "Inicia sesión primero")
        return intentar { repo.iniciarProtocolo(idVisita, idEquipo, u.id) }.also { recargar() }
    }

    /** Historial de un equipo (RF-07). El cliente solo ve protocolos Formalizados. */
    fun historialDeEquipo(idEquipo: Long): List<ProtocoloVista> {
        val soloFormalizados = _usuarioActual.value?.rol == Rol.CLIENTE
        return db.listarProtocolosDeEquipo(idEquipo)
            .filter { !soloFormalizados || it.estado == EstadoProtocolo.FORMALIZADO }
            .sortedByDescending { it.fechaHoraPrueba }
            .mapNotNull { repo.armarVista(it) }
    }

    fun nombreEquipo(idEquipo: Long): String = db.buscarEquipo(idEquipo)?.let { "${it.tag} · ${it.tipo.etiqueta}" } ?: "Equipo"
}
