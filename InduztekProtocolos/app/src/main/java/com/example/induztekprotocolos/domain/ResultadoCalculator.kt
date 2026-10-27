package com.example.induztekprotocolos.domain

import kotlin.math.abs

/** Veredicto de una prueba más el valor derivado que vale la pena mostrar (IP, % error, etc.). */
data class ResultadoCalculo(val resultado: ResultadoPrueba?, val valorDerivado: Double? = null, val etiquetaDerivada: String? = null)

/** RN-06 a RN-10 como funciones puras: sin Android ni Compose, se pueden probar con JUnit. */
object ResultadoCalculator {

    fun calcular(tipoPrueba: TipoPrueba, tipoEquipo: TipoEquipo, valores: Map<String, Double>, maxContactosUohmDePlaca: Double?): ResultadoCalculo =
        when (tipoPrueba) {
            TipoPrueba.AISLACION -> aislacion(tipoEquipo, valores)
            TipoPrueba.TTR -> ttr(valores)
            TipoPrueba.CONTACTOS -> contactos(valores, maxContactosUohmDePlaca)
            TipoPrueba.OPERACION_RELE -> operacionRele(valores)
            TipoPrueba.DEVANADOS -> ResultadoCalculo(null) // RN-08c: lo define el ingeniero a mano
        }

    // RN-06 · IP = lectura10min / lectura1min. Lectura a 1 min < 100 MΩ => Rechazado siempre.
    private fun aislacion(tipoEquipo: TipoEquipo, v: Map<String, Double>): ResultadoCalculo {
        val l1 = v["LECTURA_1MIN"] ?: return ResultadoCalculo(null)
        if (l1 < 100.0) return ResultadoCalculo(ResultadoPrueba.RECHAZADO, l1, "Lectura 1 min")
        if (!CatalogoPruebas.aislacionConIp(tipoEquipo)) return ResultadoCalculo(ResultadoPrueba.APROBADO, l1, "Lectura 1 min")
        val l10 = v["LECTURA_10MIN"] ?: return ResultadoCalculo(null)
        val ip = l10 / l1
        val r = when {
            ip >= 2.0 -> ResultadoPrueba.APROBADO
            ip >= 1.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, ip, "IP")
    }

    // RN-07 · Error % = |medida - teórica| / teórica × 100
    private fun ttr(v: Map<String, Double>): ResultadoCalculo {
        val teorica = v["RELACION_TEORICA"] ?: return ResultadoCalculo(null)
        val medida = v["RELACION_MEDIDA"] ?: return ResultadoCalculo(null)
        if (teorica == 0.0) return ResultadoCalculo(null)
        val e = abs(medida - teorica) / teorica * 100.0
        val r = when {
            e <= 0.5 -> ResultadoPrueba.APROBADO
            e <= 1.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, e, "Error %")
    }

    // RN-08 · % sobre el máximo de placa del fabricante
    private fun contactos(v: Map<String, Double>, maxDePlaca: Double?): ResultadoCalculo {
        val medido = v["RESISTENCIA_CONTACTO"] ?: return ResultadoCalculo(null)
        if (maxDePlaca == null || maxDePlaca == 0.0) return ResultadoCalculo(null)
        val pct = medido / maxDePlaca * 100.0
        val r = when {
            pct <= 100.0 -> ResultadoPrueba.APROBADO
            pct <= 120.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, pct, "% del máximo de placa")
    }

    // RN-08b · % de diferencia entre tiempo medido y esperado
    private fun operacionRele(v: Map<String, Double>): ResultadoCalculo {
        val esperado = v["TIEMPO_ESPERADO"] ?: return ResultadoCalculo(null)
        val medido = v["TIEMPO_MEDIDO"] ?: return ResultadoCalculo(null)
        if (esperado == 0.0) return ResultadoCalculo(null)
        val d = abs(medido - esperado) / esperado * 100.0
        val r = when {
            d <= 5.0 -> ResultadoPrueba.APROBADO
            d <= 10.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, d, "Diferencia %")
    }

    // RN-10 · el resultado global es el peor de sus pruebas realizadas
    fun resultadoGlobal(resultados: List<ResultadoPrueba>): ResultadoPrueba? = when {
        resultados.isEmpty() -> null
        ResultadoPrueba.RECHAZADO in resultados -> ResultadoPrueba.RECHAZADO
        ResultadoPrueba.OBSERVADO in resultados -> ResultadoPrueba.OBSERVADO
        else -> ResultadoPrueba.APROBADO
    }

    // RN-09 · variación significativa: más de 25% respecto de la prueba anterior
    fun variacionSignificativa(actual: Double, anterior: Double): Boolean =
        anterior != 0.0 && abs(actual - anterior) / abs(anterior) * 100.0 > 25.0

    fun porcentajeVariacion(actual: Double, anterior: Double): Double? =
        if (anterior == 0.0) null else (actual - anterior) / abs(anterior) * 100.0
}
