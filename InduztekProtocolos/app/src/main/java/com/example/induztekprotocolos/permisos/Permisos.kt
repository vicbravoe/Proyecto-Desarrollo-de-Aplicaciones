package com.example.induztekprotocolos.permisos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

// =====================================================================
//  NUEVO (Permisos) — Permisos.kt
//  ---------------------------------------------------------------------
//  Los 4 pasos para usar una API protegida por un permiso PELIGROSO:
//
//   1) DECLARAR   <uses-permission> en AndroidManifest.xml
//   2) VERIFICAR  ¿ya está concedido?             -> tiene()   (este archivo)
//   3) PEDIR      mostrar el diálogo del sistema   -> PerfilScreen (RequestPermission)
//   4) REACCIONAR concedido -> usar la API  /  rechazado -> avisar y ofrecer Ajustes
//
//  Si el usuario rechaza 2 veces, Android NO vuelve a mostrar el diálogo:
//  la única forma de concederlo es desde Ajustes -> abrirAjustes().
// =====================================================================

object Permisos {

    // NUEVO (Cámara): nombre del permiso de cámara (es igual en todas las versiones)
    val CAMARA: String = Manifest.permission.CAMERA

    // NUEVO (Ubicación): permiso PELIGROSO para saber dónde se hizo la prueba (GPS).
    val UBICACION: String = Manifest.permission.ACCESS_FINE_LOCATION

    // NUEVO (Galería): el permiso para leer fotos CAMBIÓ en Android 13 (API 33).
    // Build.VERSION.SDK_INT = versión de Android del teléfono donde corre la app.
    val IMAGENES: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    // NUEVO (Permisos): PASO 2 -> VERIFICAR. Devuelve true si el usuario ya lo concedió.
    // ContextCompat funciona igual en todas las versiones de Android.
    fun tiene(context: Context, permiso: String): Boolean =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    // NUEVO (Permisos): abre la pantalla de Ajustes de ESTA app (Permisos, Almacenamiento...).
    // "package:cl.induztek.protocolos" le indica a Ajustes qué app mostrar.
    fun abrirAjustes(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
