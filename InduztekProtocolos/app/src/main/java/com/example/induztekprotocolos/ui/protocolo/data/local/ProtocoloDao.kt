package cl.induztek.protocolos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {
    @Insert
    suspend fun insertar(protocolo: ProtocoloEntity): Long

    // Historial de un equipo, del más reciente al más antiguo (punto 3.2: comparar con mediciones anteriores).
    @Query("SELECT * FROM protocolos WHERE equipoId = :equipoId ORDER BY fechaHora DESC")
    fun observarHistorialDe(equipoId: Long): Flow<List<ProtocoloEntity>>

    @Query("SELECT * FROM protocolos ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<ProtocoloEntity>>

    @Query("SELECT COUNT(*) FROM protocolos")
    fun observarCantidadTotal(): Flow<Int>
}
