package cl.induztek.protocolos.data.repository

import cl.induztek.protocolos.data.local.AppDatabase
import cl.induztek.protocolos.data.local.EquipoEntity
import cl.induztek.protocolos.data.local.ProtocoloEntity
import cl.induztek.protocolos.data.local.TipoEquipo
import kotlinx.coroutines.flow.Flow

/**
 * Capa Repository del patrón MVVM: los ViewModel nunca hablan con Room
 * directamente, siempre pasan por acá. Hoy solo envuelve datos locales;
 * cuando se conecte el backend Spring Boot (siguiente fase), este es el
 * único archivo que necesita saber que existe una fuente remota también.
 */
class ProtocoloRepository(private val db: AppDatabase) {

    fun observarEquipos(): Flow<List<EquipoEntity>> = db.equipoDao().observarTodos()

    suspend fun obtenerEquipo(id: Long): EquipoEntity? = db.equipoDao().buscarPorId(id)

    fun observarHistorialDe(equipoId: Long): Flow<List<ProtocoloEntity>> =
        db.protocoloDao().observarHistorialDe(equipoId)

    fun observarCantidadProtocolos(): Flow<Int> = db.protocoloDao().observarCantidadTotal()

    suspend fun registrarProtocolo(protocolo: ProtocoloEntity): Long =
        db.protocoloDao().insertar(protocolo)

    /** Datos ficticios para que la app no arranque vacía (solo la primera vez). */
    suspend fun sembrarEquiposDeEjemploSiCorresponde() {
        if (db.equipoDao().contar() > 0) return
        db.equipoDao().insertarTodos(
            listOf(
                EquipoEntity(codigo = "TR-104", tipo = TipoEquipo.TRANSFORMADOR, instalacion = "Planta Norte — Sala eléctrica 2"),
                EquipoEntity(codigo = "RP-022", tipo = TipoEquipo.RELE_PROTECCION, instalacion = "Subestación Lo Espejo"),
                EquipoEntity(codigo = "CE-311", tipo = TipoEquipo.CELDA, instalacion = "Planta Norte — Sala eléctrica 1"),
                EquipoEntity(codigo = "CB-087", tipo = TipoEquipo.CABLE, instalacion = "Edificio administrativo, piso -1")
            )
        )
    }
}