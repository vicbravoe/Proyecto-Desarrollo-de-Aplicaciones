package com.example.induztekprotocolos.domain

/** RN-03: los RUT (clientes, técnicos, receptores) deben tener dígito verificador válido (módulo 11). */
object Rut {
    fun esValido(rutConFormato: String): Boolean {
        val limpio = rutConFormato.trim().replace(".", "").replace("-", "").uppercase()
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val cuerpoNumerico = cuerpo.toIntOrNull() ?: return false
        return calcularDv(cuerpoNumerico) == dv
    }

    private fun calcularDv(cuerpo: Int): Char {
        var suma = 0
        var multiplicador = 2
        var resto = cuerpo
        while (resto > 0) {
            suma += (resto % 10) * multiplicador
            resto /= 10
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val dv = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + dv)
        }
    }

    fun formatear(rutConFormato: String): String {
        val limpio = rutConFormato.trim().replace(".", "").replace("-", "").uppercase()
        if (limpio.length < 2) return rutConFormato
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        return cuerpo.reversed().chunked(3).joinToString(".").reversed() + "-" + dv
    }
}
