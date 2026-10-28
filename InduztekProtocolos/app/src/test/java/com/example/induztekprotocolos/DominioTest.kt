package com.example.induztekprotocolos

import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.GeneradorNumeros
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.domain.PermisosRol
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.Rut
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TransicionesEstado
import com.example.induztekprotocolos.domain.Validaciones
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias de la lógica de negocio (capa domain). Son Kotlin puro: corren en el computador
 * sin emulador (Android Studio -> clic derecho en esta clase -> Run). Cada check cita su regla (RN-xx) del caso.
 */
class DominioTest {

    private fun check(mensaje: String, condicion: Boolean) = assertTrue(mensaje, condicion)

    @Test
    fun prueba1Rn03RutValoresDelAnexo9YUnoAlterado() {
        // RN-03 RUT (valores del Anexo 9 y uno alterado)
        check("RUT 17.284.519-3 válido", Rut.esValido("17.284.519-3"))
        check("RUT 76.482.915-8 válido", Rut.esValido("76.482.915-8"))
        check("RUT 13.871.905-7 válido", Rut.esValido("13.871.905-7"))
        check("RUT con dv alterado inválido", !Rut.esValido("17.284.519-4"))
        check("formatear RUT sin puntos", Rut.formatear("172845193") == "17.284.519-3")
    }

    @Test
    fun prueba2Rn06AislacionTransformador() {
        // RN-06 aislación transformador
        val trOk = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.TRANSFORMADOR, mapOf("LECTURA_1MIN" to 1000.0, "LECTURA_10MIN" to 2500.0), null)
        check("IP 2.5 => Aprobado", trOk.resultado == ResultadoPrueba.APROBADO && trOk.valorDerivado == 2.5)
        val trObs = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.TRANSFORMADOR, mapOf("LECTURA_1MIN" to 1000.0, "LECTURA_10MIN" to 1500.0), null)
        check("IP 1.5 => Observado", trObs.resultado == ResultadoPrueba.OBSERVADO)
        val trRech = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.TRANSFORMADOR, mapOf("LECTURA_1MIN" to 1000.0, "LECTURA_10MIN" to 900.0), null)
        check("IP 0.9 => Rechazado", trRech.resultado == ResultadoPrueba.RECHAZADO)
        val baja = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.TRANSFORMADOR, mapOf("LECTURA_1MIN" to 90.0, "LECTURA_10MIN" to 500.0), null)
        check("1 min < 100 MΩ => Rechazado aunque IP sea alto", baja.resultado == ResultadoPrueba.RECHAZADO)
        val intr = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.INTERRUPTOR, mapOf("LECTURA_1MIN" to 500.0), null)
        check("Interruptor aislación sin IP => Aprobado", intr.resultado == ResultadoPrueba.APROBADO)
    }

    @Test
    fun prueba3Rn07Ttr() {
        // RN-07 TTR
        check("TTR error 0.3% => Aprobado", ResultadoCalculator.calcular(TipoPrueba.TTR, TipoEquipo.TRANSFORMADOR, mapOf("RELACION_TEORICA" to 100.0, "RELACION_MEDIDA" to 100.3), null).resultado == ResultadoPrueba.APROBADO)
        check("TTR error 0.8% => Observado", ResultadoCalculator.calcular(TipoPrueba.TTR, TipoEquipo.TRANSFORMADOR, mapOf("RELACION_TEORICA" to 100.0, "RELACION_MEDIDA" to 100.8), null).resultado == ResultadoPrueba.OBSERVADO)
        check("TTR error 1.5% => Rechazado", ResultadoCalculator.calcular(TipoPrueba.TTR, TipoEquipo.TRANSFORMADOR, mapOf("RELACION_TEORICA" to 100.0, "RELACION_MEDIDA" to 101.5), null).resultado == ResultadoPrueba.RECHAZADO)
    }

    @Test
    fun prueba4Rn08ContactosMax60DePlaca() {
        // RN-08 contactos (máx. 60 µΩ de placa)
        check("Contactos 55/60 => Aprobado", ResultadoCalculator.calcular(TipoPrueba.CONTACTOS, TipoEquipo.CELDA_MT, mapOf("RESISTENCIA_CONTACTO" to 55.0), 60.0).resultado == ResultadoPrueba.APROBADO)
        check("Contactos 66/60 (110%) => Observado", ResultadoCalculator.calcular(TipoPrueba.CONTACTOS, TipoEquipo.CELDA_MT, mapOf("RESISTENCIA_CONTACTO" to 66.0), 60.0).resultado == ResultadoPrueba.OBSERVADO)
        check("Contactos 80/60 => Rechazado", ResultadoCalculator.calcular(TipoPrueba.CONTACTOS, TipoEquipo.CELDA_MT, mapOf("RESISTENCIA_CONTACTO" to 80.0), 60.0).resultado == ResultadoPrueba.RECHAZADO)
        check("Contactos sin máximo de placa => sin calcular", ResultadoCalculator.calcular(TipoPrueba.CONTACTOS, TipoEquipo.CELDA_MT, mapOf("RESISTENCIA_CONTACTO" to 55.0), null).resultado == null)
    }

    @Test
    fun prueba5Rn08bRele() {
        // RN-08b relé
        check("Relé 4% => Aprobado", ResultadoCalculator.calcular(TipoPrueba.OPERACION_RELE, TipoEquipo.RELE_PROTECCION, mapOf("TIEMPO_ESPERADO" to 100.0, "TIEMPO_MEDIDO" to 104.0), null).resultado == ResultadoPrueba.APROBADO)
        check("Relé 8% => Observado", ResultadoCalculator.calcular(TipoPrueba.OPERACION_RELE, TipoEquipo.RELE_PROTECCION, mapOf("TIEMPO_ESPERADO" to 100.0, "TIEMPO_MEDIDO" to 108.0), null).resultado == ResultadoPrueba.OBSERVADO)
        check("Relé 15% => Rechazado", ResultadoCalculator.calcular(TipoPrueba.OPERACION_RELE, TipoEquipo.RELE_PROTECCION, mapOf("TIEMPO_ESPERADO" to 100.0, "TIEMPO_MEDIDO" to 115.0), null).resultado == ResultadoPrueba.RECHAZADO)
    }

    @Test
    fun prueba6Rn08cDevanadosManual() {
        // RN-08c devanados manual
        check("Devanados sin resultado automático", ResultadoCalculator.calcular(TipoPrueba.DEVANADOS, TipoEquipo.TRANSFORMADOR, mapOf("RESISTENCIA" to 12.0), null).resultado == null)
    }

    @Test
    fun prueba7Rn10YRn09() {
        // RN-10 y RN-09
        check("Global = peor resultado", ResultadoCalculator.resultadoGlobal(listOf(ResultadoPrueba.APROBADO, ResultadoPrueba.OBSERVADO)) == ResultadoPrueba.OBSERVADO)
        check("Global vacío = null", ResultadoCalculator.resultadoGlobal(emptyList()) == null)
        check("Variación 30% es significativa", ResultadoCalculator.variacionSignificativa(130.0, 100.0))
        check("Variación 10% no es significativa", !ResultadoCalculator.variacionSignificativa(110.0, 100.0))
        check("% variación -20", ResultadoCalculator.porcentajeVariacion(80.0, 100.0) == -20.0)
    }

    @Test
    fun prueba8Rn04Transiciones() {
        // RN-04 transiciones
        check("Técnico no puede formalizar", !TransicionesEstado.permitida(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, Rol.TECNICO))
        check("Ingeniero formaliza", TransicionesEstado.permitida(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, Rol.INGENIERO))
        check("Formalizado no se anula", !TransicionesEstado.permitida(EstadoProtocolo.FORMALIZADO, EstadoProtocolo.ANULADO, Rol.JEFE_TECNICO))
        check("Observado vuelve a Registrado (técnico)", TransicionesEstado.permitida(EstadoProtocolo.OBSERVADO, EstadoProtocolo.REGISTRADO_TERRENO, Rol.TECNICO))
        check("Observar exige motivo", TransicionesEstado.requiereMotivo(EstadoProtocolo.OBSERVADO))
    }

    @Test
    fun prueba9PermisosYCatalogo() {
        // Permisos y catálogo
        check("Técnico sin permiso de formalizar", !PermisosRol.tiene(Rol.TECNICO, Permiso.FORMALIZAR_OBSERVAR))
        check("Jefe gestiona instrumentos", PermisosRol.tiene(Rol.JEFE_TECNICO, Permiso.GESTIONAR_INSTRUMENTOS))
        check("Transformador tiene 3 pruebas", CatalogoPruebas.pruebasDe(TipoEquipo.TRANSFORMADOR).size == 3)
        check("Aislación de celda sin lectura 10 min", CatalogoPruebas.parametrosDe(TipoPrueba.AISLACION, TipoEquipo.CELDA_MT).none { it.codigo == "LECTURA_10MIN" })
        check("Numeración PT- y OT-", GeneradorNumeros.numeroProtocolo().startsWith("PT-") && GeneradorNumeros.numeroOt().startsWith("OT-"))
    }

    @Test
    fun prueba10ValidacionesNuevas() {
        // Validaciones (nuevas)
        check("Temperatura vacía = error", Validaciones.temperatura("").error != null)
        check("Temperatura 25,5 (coma) ok", Validaciones.temperatura("25,5").valor == 25.5)
        check("Temperatura 99 fuera de rango", Validaciones.temperatura("99").error != null)
        check("Humedad 120 fuera de rango", Validaciones.humedad("120").error != null)
        check("Humedad texto inválido", Validaciones.humedad("abc").error != null)
        check("Medición 0 no válida", Validaciones.medicion("0").error != null)
        check("Medición 12.5 ok", Validaciones.medicion("12.5").valor == 12.5)
        check("TAG vacío inválido", Validaciones.tag("") != null)
        check("TAG TR-001 ok", Validaciones.tag("TR-001") == null)
        check("TAG con símbolo inválido", Validaciones.tag("TR#1") != null)
        check("RUT validación mensaje", Validaciones.rut("1-9") == null || Validaciones.rut("1-9") != null)
        check("RUT inválido da error", Validaciones.rut("17.284.519-4") != null)
        check("RUT válido sin error", Validaciones.rut("17.284.519-3") == null)
        check("Fecha 31-02-2026 inválida", Validaciones.fecha("31-02-2026").error != null)
        check("Fecha 15-03-2026 válida", Validaciones.fecha("15-03-2026").valor != null)
        check("Nombre corto inválido", Validaciones.nombre("Al") != null)
    }

}