package com.example.induztekprotocolos.data

import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.GeneradorNumeros
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoCalculo
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.Rut
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.domain.TransicionesEstado
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.HistorialEstado
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Medicion
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.model.PruebaVista
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.Visita

/**
 * Aquí viven las REGLAS DE NEGOCIO (RN-xx) que necesitan la base de datos. Los ViewModel
 * le piden cosas a este repositorio y no tocan SQL; el repositorio avisa con ErrorNegocio
 * cuando una regla no se cumple. Patrón: Pantalla -> ViewModel -> Repositorio -> DbHelper.
 */
class InduztekRepositorio(val db: InduztekDbHelper) {

    // ---------------- Equipos ----------------
    /** HU-01 · RN-01: el TAG debe ser único dentro de la instalación. */
    fun registrarEquipo(idInstalacion: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?): Long {
        if (tag.isBlank()) throw ErrorNegocio("El TAG del equipo es obligatorio")
        if (db.buscarEquipoPorTag(idInstalacion, tag.trim()) != null) throw ErrorNegocio("El equipo \"${tag.trim()}\" ya existe en esta instalación")
        return db.insertarEquipo(Equipo(idInstalacion = idInstalacion, tag = tag.trim(), tipo = tipo, marca = marca.trim(), numeroSerie = serie.trim(), maxContactosUohm = maxContactos))
    }

    // ---------------- Instrumentos ----------------
    fun registrarInstrumento(nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long): Long {
        if (nombre.isBlank()) throw ErrorNegocio("El nombre es obligatorio")
        if (vencimiento <= calibracion) throw ErrorNegocio("El vencimiento debe ser posterior a la calibración")
        return db.insertarInstrumento(Instrumento(nombre = nombre.trim(), marca = marca.trim(), modelo = modelo.trim(), numeroSerie = serie.trim(), fechaUltimaCalibracion = calibracion, fechaVencimiento = vencimiento))
    }

    /** RN-11: no se puede usar un instrumento con la calibración vencida. */
    fun calibracionVigente(idInstrumento: Long, fecha: Long = System.currentTimeMillis()): Boolean =
        (db.buscarInstrumento(idInstrumento)?.fechaVencimiento ?: 0L) >= fecha

    // ---------------- Visitas ----------------
    fun iniciarVisita(idInstalacion: Long, tipo: TipoVisita, idIngeniero: Long, participantes: List<Long>): Long =
        db.insertarVisita(
            Visita(numeroOt = GeneradorNumeros.numeroOt(), idInstalacion = idInstalacion, fecha = System.currentTimeMillis(), tipoVisita = tipo, idIngenieroCargo = idIngeniero),
            participantes
        )

    /** RN-13: solo se cierra si todos los protocolos están Registrado en terreno o Anulado. RN-03: RUT válido. */
    fun cerrarVisita(idVisita: Long, receptorNombre: String, receptorRut: String, receptorCargo: String) {
        val visita = db.buscarVisita(idVisita) ?: throw ErrorNegocio("La visita no existe")
        if (visita.cerrada) throw ErrorNegocio("La visita ya está cerrada")
        val pendientes = db.listarProtocolosDeVisita(idVisita).filterNot { it.estado == EstadoProtocolo.REGISTRADO_TERRENO || it.estado == EstadoProtocolo.ANULADO }
        if (pendientes.isNotEmpty()) throw ErrorNegocio("Faltan ${pendientes.size} protocolo(s) por dejar en \"Registrado en terreno\"")
        if (receptorNombre.isBlank() || receptorCargo.isBlank()) throw ErrorNegocio("Nombre y cargo de quien recibe son obligatorios")
        if (!Rut.esValido(receptorRut)) throw ErrorNegocio("El RUT de quien recibe no es válido")
        db.cerrarVisita(idVisita, System.currentTimeMillis(), receptorNombre.trim(), Rut.formatear(receptorRut), receptorCargo.trim())
    }

    // ---------------- Protocolos ----------------
    /** Aclaración 5: un protocolo por equipo y visita; si ya existe, se reabre ese mismo. */
    fun iniciarProtocolo(idVisita: Long, idEquipo: Long, idUsuario: Long): Long {
        db.buscarProtocoloDeVisitaYEquipo(idVisita, idEquipo)?.let { return it.id }
        val visita = db.buscarVisita(idVisita) ?: throw ErrorNegocio("La visita no existe")
        if (visita.cerrada) throw ErrorNegocio("La visita está cerrada")
        return db.insertarProtocolo(Protocolo(numero = GeneradorNumeros.numeroProtocolo(), idVisita = idVisita, idEquipo = idEquipo, idUsuarioRegistra = idUsuario, fechaHoraPrueba = System.currentTimeMillis()))
    }

    /** RN-17: el técnico solo edita sus propios protocolos, y solo en Borrador u Observado. */
    fun puedeEditar(protocolo: Protocolo, usuario: Usuario): Boolean =
        TransicionesEstado.editable(protocolo.estado) && (usuario.rol != Rol.TECNICO || usuario.id == protocolo.idUsuarioRegistra) && usuario.rol != Rol.CLIENTE

    fun guardarAmbiente(p: Protocolo, temperatura: Double, humedad: Double, descripcionMantenimiento: String?) {
        db.actualizarProtocolo(p.copy(temperaturaC = temperatura, humedadPct = humedad, descripcionMantenimiento = descripcionMantenimiento?.ifBlank { null }))
    }

    fun guardarEvidencia(p: Protocolo, foto: ByteArray?, latitud: Double?, longitud: Double?) {
        db.actualizarProtocolo(p.copy(foto = foto ?: p.foto, latitud = latitud ?: p.latitud, longitud = longitud ?: p.longitud))
    }

    /** RF-04/05/06: registra una prueba con sus mediciones y calcula su resultado según RN-06 a RN-08b. */
    fun registrarPrueba(
        protocolo: Protocolo, equipo: Equipo, tipoPrueba: TipoPrueba, elemento: String, momento: MomentoPrueba,
        realizada: Boolean, motivoNoRealizada: String?, idInstrumento: Long?, valores: Map<String, Double>
    ): Long {
        if (elemento.isBlank()) throw ErrorNegocio("Indica el elemento probado (devanado, fase o polo)")
        if (!realizada && motivoNoRealizada.isNullOrBlank()) throw ErrorNegocio("Si la prueba no se realizó, el motivo es obligatorio")
        if (realizada) {
            if (idInstrumento == null) throw ErrorNegocio("Selecciona el instrumento utilizado")
            if (!calibracionVigente(idInstrumento)) throw ErrorNegocio("El instrumento seleccionado tiene la calibración vencida")
        }
        val calculo = if (realizada) ResultadoCalculator.calcular(tipoPrueba, equipo.tipo, valores, equipo.maxContactosUohm) else ResultadoCalculo(null)
        val definiciones = CatalogoPruebas.parametrosDe(tipoPrueba, equipo.tipo).associateBy { it.codigo }
        val mediciones = if (realizada) valores.mapNotNull { (codigo, valor) -> definiciones[codigo]?.let { Medicion(idPrueba = 0, parametro = codigo, valor = valor, unidad = it.unidad) } } else emptyList()
        return db.insertarPruebaConMediciones(
            Prueba(idProtocolo = protocolo.id, tipoPrueba = tipoPrueba, elemento = elemento.trim(), momento = momento, realizada = realizada,
                motivoNoRealizada = motivoNoRealizada?.trim(), idInstrumento = if (realizada) idInstrumento else null,
                resultado = calculo.resultado, valorDerivado = calculo.valorDerivado, etiquetaDerivada = calculo.etiquetaDerivada),
            mediciones
        )
    }

    /** RN-08c: la resistencia de devanados la decide el ingeniero a mano. */
    fun fijarResultadoManual(idPrueba: Long, resultado: ResultadoPrueba) { db.actualizarResultadoPrueba(idPrueba, resultado) }

    /** RN-04 + RN-17 + RNF-07: valida la transición, exige motivo y deja constancia en el historial. */
    fun cambiarEstado(protocolo: Protocolo, nuevo: EstadoProtocolo, usuario: Usuario, motivo: String? = null) {
        if (!TransicionesEstado.permitida(protocolo.estado, nuevo, usuario.rol))
            throw ErrorNegocio("No puedes mover este protocolo de \"${protocolo.estado.etiqueta}\" a \"${nuevo.etiqueta}\"")
        if (TransicionesEstado.requiereMotivo(nuevo) && motivo.isNullOrBlank()) throw ErrorNegocio("El motivo es obligatorio para pasar a \"${nuevo.etiqueta}\"")
        if (nuevo == EstadoProtocolo.REGISTRADO_TERRENO) {
            if (protocolo.temperaturaC == null || protocolo.humedadPct == null) throw ErrorNegocio("Temperatura y humedad son obligatorias antes de registrar en terreno")
            if (db.listarPruebasDeProtocolo(protocolo.id).isEmpty()) throw ErrorNegocio("Registra al menos una prueba antes de continuar")
        }
        if (nuevo == EstadoProtocolo.FORMALIZADO) {
            val sinResultado = db.listarPruebasDeProtocolo(protocolo.id).filter { it.realizada && it.resultado == null }
            if (sinResultado.isNotEmpty()) throw ErrorNegocio("Define el resultado de ${sinResultado.size} prueba(s) manual(es) (devanados) antes de formalizar")
        }
        db.actualizarProtocolo(protocolo.copy(estado = nuevo))
        db.insertarHistorialEstado(HistorialEstado(idProtocolo = protocolo.id, estadoAnterior = protocolo.estado, estadoNuevo = nuevo, idUsuario = usuario.id, fechaHora = System.currentTimeMillis(), motivo = motivo?.trim()))
    }

    // ---------------- Consultas derivadas ----------------
    /** RN-10: el resultado global es el peor de las pruebas realizadas. No se guarda, se calcula. */
    fun resultadoGlobal(idProtocolo: Long): ResultadoPrueba? =
        ResultadoCalculator.resultadoGlobal(db.listarPruebasDeProtocolo(idProtocolo).filter { it.realizada }.mapNotNull { it.resultado })

    /** RN-09: el "valor principal" de una prueba es su valor derivado (IP, %...) o, si no hay, su primera medición. */
    fun valorPrincipal(prueba: Prueba): Double? = prueba.valorDerivado ?: db.listarMedicionesDePrueba(prueba.id).firstOrNull()?.valor

    /** RN-09: la prueba anterior (más antigua que este protocolo) del mismo tipo y elemento en el mismo equipo. */
    fun buscarPruebaAnterior(protocolo: Protocolo, tipo: TipoPrueba, elemento: String): Prueba? =
        db.listarProtocolosDeEquipo(protocolo.idEquipo)
            .filter { it.fechaHoraPrueba < protocolo.fechaHoraPrueba && it.estado != EstadoProtocolo.ANULADO }
            .sortedByDescending { it.fechaHoraPrueba }
            .firstNotNullOfOrNull { anterior -> db.listarPruebasDeProtocolo(anterior.id).firstOrNull { it.tipoPrueba == tipo && it.elemento == elemento && it.realizada } }

    // ---------------- Vistas para las pantallas ----------------
    /** Junta protocolo + equipo + pruebas con su comparación contra la prueba anterior (RN-09) y el global (RN-10). */
    fun armarVista(protocolo: Protocolo): ProtocoloVista? {
        val equipo = db.buscarEquipo(protocolo.idEquipo) ?: return null
        val pruebas = db.listarPruebasDeProtocolo(protocolo.id).map { prueba ->
            val actual = valorPrincipal(prueba)
            val anterior = if (prueba.realizada) buscarPruebaAnterior(protocolo, prueba.tipoPrueba, prueba.elemento)?.let { valorPrincipal(it) } else null
            val variacion = if (actual != null && anterior != null) ResultadoCalculator.porcentajeVariacion(actual, anterior) else null
            PruebaVista(
                prueba = prueba,
                mediciones = db.listarMedicionesDePrueba(prueba.id),
                instrumento = prueba.idInstrumento?.let { db.buscarInstrumento(it)?.nombre },
                valorActual = actual,
                valorAnterior = anterior,
                variacionPct = variacion,
                significativa = actual != null && anterior != null && ResultadoCalculator.variacionSignificativa(actual, anterior)
            )
        }
        return ProtocoloVista(protocolo, equipo, pruebas, resultadoGlobal(protocolo.id))
    }
}
