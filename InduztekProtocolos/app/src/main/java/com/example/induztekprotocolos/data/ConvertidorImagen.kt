package com.example.induztekprotocolos.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

// =====================================================================
//  NUEVO (Avatar) — ConvertidorImagen.kt
//  ---------------------------------------------------------------------
//  Una imagen pasa por 3 formas distintas en esta app:
//
//   Cámara ──▶ Bitmap ─┐
//                       ├─▶ ByteArray (JPEG) ──▶ columna BLOB de SQLite
//   Galería ─▶ Uri ────┘          │
//                                  └─▶ Bitmap ──▶ Image en el Perfil
//
//   Uri       = "dirección" de un archivo que pertenece a OTRA app (la galería).
//   Bitmap    = la imagen en memoria, pixel por pixel (pesada).
//   ByteArray = la imagen comprimida en JPEG (liviana): es lo que se guarda.
//
//  "object" = una sola instancia para toda la app (como una clase con métodos
//  estáticos en Java). Se usa así: ConvertidorImagen.aBytes(foto)
// =====================================================================

object ConvertidorImagen {

    // NUEVO (Avatar): tamaño máximo del lado más largo, en pixeles.
    // Una foto de evidencia no necesita 4000 px, pero sí se debe poder leer la placa del equipo: 800 px alcanza.
    private const val LADO_MAXIMO = 800

    // NUEVO (Avatar): calidad JPEG de 0 a 100. 80 = buen equilibrio peso / calidad.
    private const val CALIDAD_JPEG = 80

    // NUEVO (Galería): lee la imagen a la que apunta el Uri y la convierte en Bitmap.
    // contentResolver es el "intermediario" que nos deja leer archivos de otras apps.
    fun desdeUri(context: Context, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9+: ImageDecoder respeta la rotación de la foto (EXIF).
            // ALLOCATOR_SOFTWARE = Bitmap en memoria normal, para poder achicarlo después.
            val origen = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(origen) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            // Android 7 y 8: se abre el archivo como un flujo de bytes y se decodifica
            context.contentResolver.openInputStream(uri).use { flujo ->
                BitmapFactory.decodeStream(flujo)
            }
        }
    } catch (e: Exception) {
        null   // archivo dañado o sin acceso: no se cambia el avatar
    }

    // NUEVO (Avatar): Bitmap -> ByteArray (JPEG achicado). Esto es lo que va a SQLite.
    fun aBytes(bitmap: Bitmap): ByteArray {
        val chico = achicar(bitmap)
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    // NUEVO (Avatar): ByteArray -> Bitmap, para MOSTRAR la foto leída de la BD
    fun aBitmap(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // NUEVO (Avatar): reduce el tamaño manteniendo la proporción (no deforma la foto)
    private fun achicar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true    // true = suavizado (filtro bilineal)
        )
    }
}
