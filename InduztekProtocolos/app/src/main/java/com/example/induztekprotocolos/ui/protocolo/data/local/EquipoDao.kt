package cl.induztek.protocolos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    // Flow: la UI se re-dibuja sola cada vez que cambian los equipos, sin pedirlo manualmente.
    @Query("SELECT * FROM equipos ORDER BY codigo ASC")
    fun observarTodos(): Flow<List<EquipoEntity>>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun buscarPorId(id: Long): EquipoEntity?

    @Query("SELECT COUNT(*) FROM equipos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(equipos: List<EquipoEntity>)
}