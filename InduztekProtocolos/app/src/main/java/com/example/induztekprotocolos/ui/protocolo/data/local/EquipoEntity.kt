package cl.induztek.protocolos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoEquipo(val etiqueta: String) {
    TRANSFORMADOR("Transformador"),
    RELE_PROTECCION("Relé de protección"),
    CELDA("Celda"),
    CABLE("Cable")
}

/**
 * Un equipo eléctrico de un cliente (punto 3.2 del caso: "identificador o código
 * del equipo probado; tipo de equipo; instalación o ubicación del cliente").
 */
@Entity(tableName = "equipos")
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacion: String
)
