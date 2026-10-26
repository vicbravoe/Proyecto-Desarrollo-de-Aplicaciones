package cl.induztek.protocolos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ResultadoPrueba(val etiqueta: String) {
    APROBADO("Aprobado"),
    OBSERVADO("Observado"),
    RECHAZADO("Rechazado")
}

/**
 * Punto 3.2 del caso: "estado del protocolo (registrado en terreno / formalizado)".
 * Hoy el técnico solo deja REGISTRADO_EN_TERRENO; FORMALIZADO quedará para cuando
 * se conecte con el backend Spring Boot (siguiente fase del proyecto).
 */
enum class EstadoProtocolo(val etiqueta: String) {
    REGISTRADO_EN_TERRENO("Registrado en terreno"),
    FORMALIZADO("Formalizado")
}

/**
 * Un protocolo = una prueba eléctrica hecha a un [EquipoEntity] en un momento dado.
 * fechaHora se guarda como epoch millis (Long) porque Room no sabe persistir
 * LocalDateTime directamente sin un TypeConverter adicional.
 */
@Entity(
    tableName = "protocolos",
    foreignKeys = [
        ForeignKey(
            entity = EquipoEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("equipoId")]
)
data class ProtocoloEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipoId: Long,
    val fechaHora: Long,
    val valorMedicion: Double,
    val unidadMedicion: String,
    val resultado: ResultadoPrueba,
    val estado: EstadoProtocolo = EstadoProtocolo.REGISTRADO_EN_TERRENO,
    val observaciones: String = "",
    // Recurso nativo 1 (cámara): evidencia fotográfica comprimida en JPEG.
    val fotoEvidencia: ByteArray? = null,
    // Recurso nativo 2 (ubicación): dónde se realizó la prueba.
    val latitud: Double? = null,
    val longitud: Double? = null
) {
    // Room ignora equals/hashCode generados por data class para ByteArray mal comparado,
    // así que los definimos explícitamente para que el estado de la UI compare bien.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProtocoloEntity) return false
        return id == other.id && equipoId == other.equipoId && fechaHora == other.fechaHora &&
                valorMedicion == other.valorMedicion && unidadMedicion == other.unidadMedicion &&
                resultado == other.resultado && estado == other.estado &&
                observaciones == other.observaciones &&
                (fotoEvidencia?.contentEquals(other.fotoEvidencia) ?: (other.fotoEvidencia == null)) &&
                latitud == other.latitud && longitud == other.longitud
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + equipoId.hashCode()
        result = 31 * result + fechaHora.hashCode()
        result = 31 * result + valorMedicion.hashCode()
        result = 31 * result + (fotoEvidencia?.contentHashCode() ?: 0)
        return result
    }
}