package com.example.induztekprotocolos.permisos

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

// =====================================================================
//  Recurso nativo: UBICACIÓN (GPS). Usa el LocationManager de Android, sin librerías extra.
//  Es el 3er permiso PELIGROSO de la app (los otros dos: cámara y galería, como en clases).
//  Los 4 pasos son los mismos: declarar (Manifest) -> verificar (Permisos.tiene) -> pedir -> reaccionar.
// =====================================================================
object Ubicacion {

    /** Entrega la ubicación actual (o la última conocida). Entrega null si no hay permiso, GPS apagado o falla. */
    @SuppressLint("MissingPermission")   // el permiso se verifica justo aquí abajo con Permisos.tiene
    fun obtener(context: Context, alTerminar: (Location?) -> Unit) {
        if (!Permisos.tiene(context, Permisos.UBICACION)) {
            alTerminar(null)
            return
        }
        try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val proveedores = manager.getProviders(true)
            if (proveedores.isEmpty()) {
                alTerminar(null)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val proveedor = if (LocationManager.GPS_PROVIDER in proveedores) LocationManager.GPS_PROVIDER else proveedores.first()
                manager.getCurrentLocation(proveedor, null, ContextCompat.getMainExecutor(context)) { ubicacion ->
                    alTerminar(ubicacion ?: ultimaConocida(manager, proveedores))
                }
            } else {
                alTerminar(ultimaConocida(manager, proveedores))
            }
        } catch (e: SecurityException) {
            alTerminar(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(manager: LocationManager, proveedores: List<String>): Location? =
        proveedores.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
}
