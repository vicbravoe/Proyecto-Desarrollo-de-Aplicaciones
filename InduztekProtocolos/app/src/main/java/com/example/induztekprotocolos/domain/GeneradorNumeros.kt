package com.example.induztekprotocolos.domain

import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

/**
 * RN-02: no pueden existir dos protocolos con el mismo número, aunque se registren sin
 * conexión. Sin servidor que reparta un correlativo, se usa fecha + sufijo aleatorio corto.
 */
object GeneradorNumeros {
    private fun fecha(t: Long) = SimpleDateFormat("yyyyMMdd", Locale.forLanguageTag("es-CL")).format(t)

    fun numeroProtocolo(t: Long = System.currentTimeMillis()) = "PT-${fecha(t)}-${Random.nextInt(1000, 9999)}"
    fun numeroOt(t: Long = System.currentTimeMillis()) = "OT-${fecha(t)}-${Random.nextInt(100, 999)}"
}
