package com.example.induztekprotocolos.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.induztekprotocolos.data.ConvertidorImagen
import com.example.induztekprotocolos.data.InduztekDbHelper
import com.example.induztekprotocolos.data.InduztekRepositorio
import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.domain.Validaciones
import com.example.induztekprotocolos.model.FormAmbiente
import com.example.induztekprotocolos.model.FormPrueba
import com.example.induztekprotocolos.model.HistorialItem
import com.example.induztekprotocolos.model.ProtocoloUiState
import com.example.induztekprotocolos.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lógica de la pantalla de protocolo (IE 2.2.1). La pantalla NO valida ni calcula nada:
 * avisa lo que escribió el usuario (onXxx) y dibuja el UiState que sale de aquí.
 */
class ProtocoloViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper.obtener(application)
    private val repo = InduztekRepositorio(db)

    private val _uiState = MutableStateFlow(ProtocoloUiState())
    val uiState: StateFlow<ProtocoloUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var idProtocolo = 0L

    fun cargar(id: Long, usuarioActual: Usuario) {
        if (idProtocolo == id && _uiState.value.vista != null) return   // al rotar no se pierde lo escrito
        usuario = usuarioActual
        idProtocolo = id
        refrescar(primeraVez = true)
    }

    private fun formato(d: Double): String = if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

    private fun refrescar(primeraVez: Boolean = false) {
        val protocolo = db.buscarProtocolo(idProtocolo) ?: return
        val vista = repo.armarVista(protocolo) ?: return
        val u = usuario
        _uiState.update { actual ->
            actual.copy(
                vista = vista,
                visita = db.buscarVisita(protocolo.idVisita),
                rol = u?.rol,
                puedeEditar = u != null && repo.puedeEditar(protocolo, u),
                instrumentos = db.listarInstrumentos(),
                historial = db.listarHistorialEstados(protocolo.id).map { HistorialItem(it, db.buscarUsuario(it.idUsuario)?.nombre ?: "—") },
                ambiente = if (primeraVez) FormAmbiente(
                    temperatura = protocolo.temperaturaC?.let(::formato) ?: "",
                    humedad = protocolo.humedadPct?.let(::formato) ?: "",
                    descripcion = protocolo.descripcionMantenimiento ?: ""
                ) else actual.ambiente
            )
        }
    }

    private fun avisar(texto: String) = _uiState.update { it.copy(mensaje = texto) }
    private fun guardado(texto: String) = _uiState.update { it.copy(mensaje = texto, guardados = it.guardados + 1) }
    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    // ------------------------------- AMBIENTE -------------------------------
    fun onTemperatura(texto: String) = _uiState.update { it.copy(ambiente = it.ambiente.copy(temperatura = texto, errorTemperatura = null)) }
    fun onHumedad(texto: String) = _uiState.update { it.copy(ambiente = it.ambiente.copy(humedad = texto, errorHumedad = null)) }
    fun onDescripcion(texto: String) = _uiState.update { it.copy(ambiente = it.ambiente.copy(descripcion = texto)) }

    fun guardarAmbiente() {
        val estado = _uiState.value
        val protocolo = estado.vista?.protocolo ?: return
        val temp = Validaciones.temperatura(estado.ambiente.temperatura)
        val hum = Validaciones.humedad(estado.ambiente.humedad)
        _uiState.update { it.copy(ambiente = it.ambiente.copy(errorTemperatura = temp.error, errorHumedad = hum.error)) }
        if (temp.valor == null || hum.valor == null) return
        repo.guardarAmbiente(protocolo, temp.valor, hum.valor, estado.ambiente.descripcion)
        refrescar()
        guardado("Condiciones ambientales guardadas")
    }

    // ------------------------------- EVIDENCIA (recursos nativos) -------------------------------
    fun onFotoCamara(foto: Bitmap) = guardarFoto(ConvertidorImagen.aBytes(foto))

    fun onFotoGaleria(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri)
        if (imagen == null) avisar("No se pudo leer la imagen elegida") else guardarFoto(ConvertidorImagen.aBytes(imagen))
    }

    private fun guardarFoto(bytes: ByteArray) {
        val protocolo = _uiState.value.vista?.protocolo ?: return
        if (!_uiState.value.puedeEditar) { avisar("No puedes modificar este protocolo"); return }
        repo.guardarEvidencia(protocolo, bytes, null, null)
        refrescar()
        guardado("Foto de evidencia guardada")
    }

    fun onUbicacion(latitud: Double, longitud: Double) {
        val protocolo = _uiState.value.vista?.protocolo ?: return
        if (!_uiState.value.puedeEditar) { avisar("No puedes modificar este protocolo"); return }
        repo.guardarEvidencia(protocolo, null, latitud, longitud)
        refrescar()
        guardado("Ubicación registrada")
    }

    fun onUbicacionNoDisponible() = avisar("No hay ubicación disponible. Activa el GPS e inténtalo de nuevo")

    // ------------------------------- PRUEBAS -------------------------------
    fun abrirFormPrueba() = _uiState.update { it.copy(prueba = FormPrueba(visible = true, momento = momentoInicial())) }
    fun cerrarFormPrueba() = _uiState.update { it.copy(prueba = FormPrueba()) }

    fun onTipoPrueba(tipo: TipoPrueba) = actualizarForm { FormPrueba(visible = true, tipo = tipo, momento = momentoInicial(), elemento = CatalogoPruebas.elementosSugeridos(tipo, equipoTipo()).first()) }
    fun onElemento(texto: String) = actualizarForm { it.copy(elemento = texto, errorElemento = null) }
    fun onMomento(momento: MomentoPrueba) = actualizarForm { it.copy(momento = momento) }
    fun onRealizada(valor: Boolean) = actualizarForm { it.copy(realizada = valor, errorGeneral = null) }
    fun onMotivo(texto: String) = actualizarForm { it.copy(motivo = texto, errorMotivo = null) }
    fun onInstrumento(id: Long) = actualizarForm { it.copy(idInstrumento = id, errorInstrumento = null) }

    fun onValor(codigo: String, texto: String) = actualizarForm { f ->
        val nuevos = f.valores + (codigo to texto)
        f.copy(valores = nuevos, erroresValores = f.erroresValores - codigo, estimado = estimar(f.tipo, nuevos))
    }

    /** Aclaración 4: en una visita con mantenimiento las pruebas son previas o posteriores; si no, únicas. */
    private fun momentoInicial() =
        if (_uiState.value.visita?.tipoVisita == TipoVisita.PRUEBAS_Y_MANTENIMIENTO) MomentoPrueba.PREVIA else MomentoPrueba.UNICA

    private fun equipoTipo() = _uiState.value.vista!!.equipo.tipo

    private fun actualizarForm(cambio: (FormPrueba) -> FormPrueba) = _uiState.update { it.copy(prueba = cambio(it.prueba)) }

    /** Resultado "en vivo" mientras se escribe: solo si todos los parámetros ya son números válidos. */
    private fun estimar(tipo: TipoPrueba?, valores: Map<String, String>) = run {
        val equipo = _uiState.value.vista?.equipo
        if (tipo == null || equipo == null || CatalogoPruebas.esManual(tipo)) return@run null
        val definiciones = CatalogoPruebas.parametrosDe(tipo, equipo.tipo)
        val numeros = definiciones.mapNotNull { d -> Validaciones.medicion(valores[d.codigo].orEmpty()).valor?.let { d.codigo to it } }.toMap()
        if (numeros.size != definiciones.size) null
        else ResultadoCalculator.calcular(tipo, equipo.tipo, numeros, equipo.maxContactosUohm)
    }

    fun guardarPrueba() {
        val estado = _uiState.value
        val vista = estado.vista ?: return
        val f = estado.prueba
        val tipo = f.tipo
        val errorTipo = if (tipo == null) "Selecciona la prueba" else null
        val errorElemento = Validaciones.requerido(f.elemento, "El elemento")
        val errorMotivo = if (!f.realizada) Validaciones.requerido(f.motivo, "El motivo") else null
        val errorInstrumento = if (f.realizada && f.idInstrumento == null) "Selecciona el instrumento" else null
        val parseos = if (f.realizada && tipo != null)
            CatalogoPruebas.parametrosDe(tipo, vista.equipo.tipo).associate { it.codigo to Validaciones.medicion(f.valores[it.codigo].orEmpty()) }
        else emptyMap()
        val erroresValores = parseos.mapNotNull { (codigo, p) -> p.error?.let { codigo to it } }.toMap()

        if (errorTipo != null || errorElemento != null || errorMotivo != null || errorInstrumento != null || erroresValores.isNotEmpty()) {
            actualizarForm { it.copy(errorTipo = errorTipo, errorElemento = errorElemento, errorMotivo = errorMotivo, errorInstrumento = errorInstrumento, erroresValores = erroresValores) }
            return
        }
        try {
            repo.registrarPrueba(
                vista.protocolo, vista.equipo, tipo!!, f.elemento, f.momento, f.realizada,
                if (f.realizada) null else f.motivo, f.idInstrumento,
                parseos.mapValues { it.value.valor!! }
            )
            cerrarFormPrueba()
            refrescar()
            guardado("Prueba guardada")
        } catch (e: ErrorNegocio) {
            actualizarForm { it.copy(errorGeneral = e.message) }
        }
    }

    /** RN-08c: el ingeniero decide a mano el resultado de la resistencia de devanados. */
    fun fijarResultado(idPrueba: Long, resultado: ResultadoPrueba) {
        repo.fijarResultadoManual(idPrueba, resultado)
        refrescar()
        guardado("Resultado definido: ${resultado.etiqueta}")
    }

    // ------------------------------- ESTADO (RN-04) -------------------------------
    fun cambiarEstado(nuevo: EstadoProtocolo, motivo: String?) {
        val protocolo = _uiState.value.vista?.protocolo ?: return
        val u = usuario ?: return
        try {
            repo.cambiarEstado(protocolo, nuevo, u, motivo)
            _uiState.update { it.copy(errorEstado = null) }
            refrescar()
            guardado("Estado: ${nuevo.etiqueta}")
        } catch (e: ErrorNegocio) {
            _uiState.update { it.copy(errorEstado = e.message) }
        }
    }

    fun errorEstadoVisto() = _uiState.update { it.copy(errorEstado = null) }
}
