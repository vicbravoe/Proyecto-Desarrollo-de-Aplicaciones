package cl.induztek.protocolos

import android.app.Application
import cl.induztek.protocolos.data.local.AppDatabase
import cl.induztek.protocolos.data.repository.ProtocoloRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Punto de entrada del proceso. Arma, a mano, las instancias únicas que
 * necesita toda la app (base de datos y repositorio) — es un "contenedor
 * de dependencias" simple, el paso previo a usar Hilt más adelante.
 */
class InduztekApp : Application() {
    private val scopeDeApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: ProtocoloRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.obtener(this)
        repository = ProtocoloRepository(database)
        scopeDeApp.launch { repository.sembrarEquiposDeEjemploSiCorresponde() }
    }
}
