package com.example.induztekprotocolos.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.HistorialEstado
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Medicion
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.Visita
import java.util.concurrent.TimeUnit

// =====================================================================
//  InduztekDbHelper — misma técnica que UsuarioDbHelper de MiRegistro:
//  SQLiteOpenHelper + SQL a mano + ContentValues + Cursor. Cambia solo
//  el tamaño: 10 tablas en vez de 2 (núcleo del MER de Induztek).
//
//   usuarios ─┬─ instalaciones ── equipos
//             │        └──────── visitas ── protocolos ── pruebas ── mediciones
//             └─ visita_participantes      └─ historial_estados
//   instrumentos (se referencian desde pruebas)
//
//  Los enums (Rol, TipoEquipo, EstadoProtocolo...) se guardan como TEXT con su nombre.
// =====================================================================
class InduztekDbHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztek.db"
        // Igual que en MiRegistro: si cambia una tabla, se sube la versión y se migra en onUpgrade.
        private const val VERSION_BD = 1

        // Una sola conexión a la BD para toda la app (varios ViewModel comparten el mismo helper).
        @Volatile private var instancia: InduztekDbHelper? = null

        fun obtener(context: Context): InduztekDbHelper =
            instancia ?: synchronized(this) {
                instancia ?: InduztekDbHelper(context.applicationContext).also { instancia = it }
            }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)   // sin esto SQLite ignora las claves foráneas
    }

    override fun onCreate(db: SQLiteDatabase) {
        TABLAS.forEach { db.execSQL(it) }
        sembrar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Próxima migración: if (oldVersion < 2) { db.execSQL("ALTER TABLE ...") }
    }

    private val TABLAS = listOf(
        "CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, rol TEXT NOT NULL, rut TEXT NOT NULL UNIQUE, " +
            "nombre TEXT NOT NULL, correo TEXT NOT NULL UNIQUE, telefono TEXT NOT NULL DEFAULT '', activo INTEGER NOT NULL DEFAULT 1, " +
            "giro TEXT, contacto_nombre TEXT, contacto_cargo TEXT)",
        "CREATE TABLE instalaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario_cliente INTEGER REFERENCES usuarios(id) ON DELETE SET NULL, " +
            "nombre TEXT NOT NULL, comuna TEXT NOT NULL, observaciones_acceso TEXT NOT NULL DEFAULT '')",
        "CREATE TABLE equipos (id INTEGER PRIMARY KEY AUTOINCREMENT, id_instalacion INTEGER NOT NULL REFERENCES instalaciones(id) ON DELETE CASCADE, " +
            "tag TEXT NOT NULL, tipo TEXT NOT NULL, marca TEXT NOT NULL DEFAULT '', numero_serie TEXT NOT NULL DEFAULT '', " +
            "tensiones TEXT NOT NULL DEFAULT '', max_contactos_uohm REAL, UNIQUE (id_instalacion, tag))",
        "CREATE TABLE instrumentos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, marca TEXT NOT NULL DEFAULT '', " +
            "modelo TEXT NOT NULL DEFAULT '', numero_serie TEXT NOT NULL DEFAULT '', fecha_ultima_calibracion INTEGER NOT NULL, fecha_vencimiento INTEGER NOT NULL)",
        "CREATE TABLE visitas (id INTEGER PRIMARY KEY AUTOINCREMENT, numero_ot TEXT NOT NULL UNIQUE, " +
            "id_instalacion INTEGER NOT NULL REFERENCES instalaciones(id) ON DELETE CASCADE, fecha INTEGER NOT NULL, tipo_visita TEXT NOT NULL, " +
            "id_ingeniero_cargo INTEGER NOT NULL REFERENCES usuarios(id), fecha_hora_cierre INTEGER, receptor_nombre TEXT, receptor_rut TEXT, receptor_cargo TEXT)",
        "CREATE TABLE visita_participantes (id_visita INTEGER NOT NULL REFERENCES visitas(id) ON DELETE CASCADE, " +
            "id_usuario INTEGER NOT NULL REFERENCES usuarios(id), PRIMARY KEY (id_visita, id_usuario))",
        "CREATE TABLE protocolos (id INTEGER PRIMARY KEY AUTOINCREMENT, numero TEXT NOT NULL UNIQUE, " +
            "id_visita INTEGER NOT NULL REFERENCES visitas(id) ON DELETE CASCADE, id_equipo INTEGER NOT NULL REFERENCES equipos(id) ON DELETE CASCADE, " +
            "id_usuario_registra INTEGER NOT NULL REFERENCES usuarios(id), fecha_hora_prueba INTEGER NOT NULL, temperatura_c REAL, humedad_pct REAL, " +
            "descripcion_mantenimiento TEXT, estado TEXT NOT NULL, foto BLOB, latitud REAL, longitud REAL, UNIQUE (id_visita, id_equipo))",
        "CREATE TABLE historial_estados (id INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL REFERENCES protocolos(id) ON DELETE CASCADE, " +
            "estado_anterior TEXT, estado_nuevo TEXT NOT NULL, id_usuario INTEGER NOT NULL REFERENCES usuarios(id), fecha_hora INTEGER NOT NULL, motivo TEXT)",
        "CREATE TABLE pruebas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_protocolo INTEGER NOT NULL REFERENCES protocolos(id) ON DELETE CASCADE, " +
            "tipo_prueba TEXT NOT NULL, elemento TEXT NOT NULL, momento TEXT NOT NULL, realizada INTEGER NOT NULL, motivo_no_realizada TEXT, " +
            "id_instrumento INTEGER REFERENCES instrumentos(id) ON DELETE SET NULL, resultado TEXT, valor_derivado REAL, etiqueta_derivada TEXT)",
        "CREATE TABLE mediciones (id INTEGER PRIMARY KEY AUTOINCREMENT, id_prueba INTEGER NOT NULL REFERENCES pruebas(id) ON DELETE CASCADE, " +
            "parametro TEXT NOT NULL, valor REAL NOT NULL, unidad TEXT NOT NULL, UNIQUE (id_prueba, parametro))"
    )

    // ------------------------------------------------------------------
    //  Datos de ejemplo (Anexo 9 del caso). Todo ficticio.
    // ------------------------------------------------------------------
    private fun sembrar(db: SQLiteDatabase) {
        fun personal(rol: Rol, rut: String, nombre: String, correo: String) = db.insert("usuarios", null, ContentValues().apply {
            put("rol", rol.name); put("rut", rut); put("nombre", nombre); put("correo", correo)
        })
        fun cliente(rut: String, razon: String, correo: String, giro: String, contacto: String, cargo: String) =
            db.insert("usuarios", null, ContentValues().apply {
                put("rol", Rol.CLIENTE.name); put("rut", rut); put("nombre", razon); put("correo", correo)
                put("giro", giro); put("contacto_nombre", contacto); put("contacto_cargo", cargo)
            })
        fun instalacion(idCliente: Long, nombre: String, comuna: String, obs: String) = db.insert("instalaciones", null, ContentValues().apply {
            put("id_usuario_cliente", idCliente); put("nombre", nombre); put("comuna", comuna); put("observaciones_acceso", obs)
        })
        fun equipo(idInst: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, tensiones: String, maxContactos: Double? = null) =
            db.insert("equipos", null, ContentValues().apply {
                put("id_instalacion", idInst); put("tag", tag); put("tipo", tipo.name); put("marca", marca)
                put("numero_serie", serie); put("tensiones", tensiones); put("max_contactos_uohm", maxContactos)
            })
        fun instrumento(nombre: String, marca: String, modelo: String, serie: String, ultima: Long, vence: Long) =
            db.insert("instrumentos", null, ContentValues().apply {
                put("nombre", nombre); put("marca", marca); put("modelo", modelo); put("numero_serie", serie)
                put("fecha_ultima_calibracion", ultima); put("fecha_vencimiento", vence)
            })

        personal(Rol.TECNICO, "17.284.519-3", "Matías Godoy Herrera", "mgodoy@induztek.ejemplo")
        personal(Rol.TECNICO, "19.412.087-7", "Javiera Tapia Muñoz", "jtapia@induztek.ejemplo")
        personal(Rol.TECNICO, "26.104.583-4", "Carlos Rondón Pérez", "crondon@induztek.ejemplo")
        personal(Rol.INGENIERO, "15.906.334-8", "Andrea Contreras Silva", "acontreras@induztek.ejemplo")
        personal(Rol.INGENIERO, "16.553.270-8", "Sebastián Morales Gutiérrez", "smorales@induztek.ejemplo")
        personal(Rol.JEFE_TECNICO, "13.871.905-7", "Ricardo Espinoza Lagos", "respinoza@induztek.ejemplo")

        val envasadora = cliente("76.482.915-8", "Envasadora del Pacífico S.A.", "mfuentes@envpacifico.ejemplo", "Agroindustria", "Marcela Fuentes Rojas", "Jefa de Mantenimiento")
        val clinica = cliente("76.903.218-5", "Clínica Los Aromos Ltda.", "cmendez@losaromos.ejemplo", "Salud", "Carolina Méndez Pizarro", "Supervisor Eléctrico")
        val minera = cliente("77.105.362-9", "Minera Cerro Alto SpA", "pnavarro@cerroalto.ejemplo", "Minería", "Patricio Navarro Soto", "Jefe de Mantenimiento Eléctrico")

        val lampa = instalacion(envasadora, "Planta Lampa", "Lampa", "Inducción obligatoria de 30 min")
        val clinico = instalacion(clinica, "Edificio Clínico", "La Florida", "Cortes solo domingos 06:00–10:00")
        val tilTil = instalacion(minera, "Faena Til Til", "Til Til", "Sin señal en toda la faena")

        equipo(lampa, "TR-01", TipoEquipo.TRANSFORMADOR, "Rhona", "RH-18733", "12 / 0,4 kV")
        equipo(lampa, "TR-02", TipoEquipo.TRANSFORMADOR, "Rhona", "RH-21904", "12 / 0,4 kV")
        equipo(clinico, "CEL-MT-03", TipoEquipo.CELDA_MT, "Ormazabal", "OR-77812", "12 kV, 630 A", 60.0)
        equipo(clinico, "RP-21", TipoEquipo.RELE_PROTECCION, "SEL", "SEL-50321", "—")
        equipo(tilTil, "INT-52-1", TipoEquipo.INTERRUPTOR, "Schneider", "SE-449021", "23 kV, 1.250 A", 45.0)
        equipo(tilTil, "CAB-MT-02", TipoEquipo.CABLE_POTENCIA, "Nexans", "NX-2201-B", "15 kV, 3×240 mm², 180 m")

        val dia = TimeUnit.DAYS.toMillis(1)
        val hoy = System.currentTimeMillis()
        instrumento("Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821", hoy - 210 * dia, hoy + 155 * dia)
        instrumento("Megóhmetro 10 kV", "Megger", "MIT1025", "MIT-0071233", hoy - 360 * dia, hoy + 5 * dia)      // vence en 5 días
        instrumento("Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044", hoy - 120 * dia, hoy + 245 * dia)
        instrumento("Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882", hoy - 400 * dia, hoy - 30 * dia)        // vencido a propósito (RN-11)
        instrumento("Equipo de inyección secundaria", "Omicron", "CMC 356", "CMC-HB411", hoy - 270 * dia, hoy + 95 * dia)
    }

    // ------------------------------------------------------------------
    //  Usuarios
    // ------------------------------------------------------------------
    fun listarUsuariosActivos(): List<Usuario> = consultar("SELECT * FROM usuarios WHERE activo = 1 ORDER BY rol, nombre", null, ::leerUsuario)
    fun listarPersonalPorRol(rol: Rol): List<Usuario> =
        consultar("SELECT * FROM usuarios WHERE rol = ? AND activo = 1 ORDER BY nombre", arrayOf(rol.name), ::leerUsuario)
    fun buscarUsuario(id: Long): Usuario? = consultar("SELECT * FROM usuarios WHERE id = ?", arrayOf(id.toString()), ::leerUsuario).firstOrNull()

    // ------------------------------------------------------------------
    //  Instalaciones y equipos
    // ------------------------------------------------------------------
    fun listarInstalaciones(): List<Instalacion> = consultar("SELECT * FROM instalaciones ORDER BY nombre", null, ::leerInstalacion)
    fun listarInstalacionesDeCliente(idCliente: Long): List<Instalacion> =
        consultar("SELECT * FROM instalaciones WHERE id_usuario_cliente = ? ORDER BY nombre", arrayOf(idCliente.toString()), ::leerInstalacion)
    fun buscarInstalacion(id: Long): Instalacion? =
        consultar("SELECT * FROM instalaciones WHERE id = ?", arrayOf(id.toString()), ::leerInstalacion).firstOrNull()

    fun listarEquiposDeInstalacion(idInstalacion: Long): List<Equipo> =
        consultar("SELECT * FROM equipos WHERE id_instalacion = ? ORDER BY tag", arrayOf(idInstalacion.toString()), ::leerEquipo)
    fun buscarEquipo(id: Long): Equipo? = consultar("SELECT * FROM equipos WHERE id = ?", arrayOf(id.toString()), ::leerEquipo).firstOrNull()
    fun buscarEquipoPorTag(idInstalacion: Long, tag: String): Equipo? =
        consultar("SELECT * FROM equipos WHERE id_instalacion = ? AND tag = ? COLLATE NOCASE", arrayOf(idInstalacion.toString(), tag), ::leerEquipo).firstOrNull()

    fun insertarEquipo(e: Equipo): Long = writableDatabase.insert("equipos", null, ContentValues().apply {
        put("id_instalacion", e.idInstalacion); put("tag", e.tag); put("tipo", e.tipo.name); put("marca", e.marca)
        put("numero_serie", e.numeroSerie); put("tensiones", e.tensiones); put("max_contactos_uohm", e.maxContactosUohm)
    })

    // ------------------------------------------------------------------
    //  Instrumentos
    // ------------------------------------------------------------------
    fun listarInstrumentos(): List<Instrumento> = consultar("SELECT * FROM instrumentos ORDER BY nombre", null, ::leerInstrumento)
    fun buscarInstrumento(id: Long): Instrumento? =
        consultar("SELECT * FROM instrumentos WHERE id = ?", arrayOf(id.toString()), ::leerInstrumento).firstOrNull()

    fun insertarInstrumento(i: Instrumento): Long = writableDatabase.insert("instrumentos", null, ContentValues().apply {
        put("nombre", i.nombre); put("marca", i.marca); put("modelo", i.modelo); put("numero_serie", i.numeroSerie)
        put("fecha_ultima_calibracion", i.fechaUltimaCalibracion); put("fecha_vencimiento", i.fechaVencimiento)
    })

    // ------------------------------------------------------------------
    //  Visitas
    // ------------------------------------------------------------------
    fun listarVisitas(): List<Visita> = consultar("SELECT * FROM visitas ORDER BY fecha DESC", null, ::leerVisita)
    fun buscarVisita(id: Long): Visita? = consultar("SELECT * FROM visitas WHERE id = ?", arrayOf(id.toString()), ::leerVisita).firstOrNull()

    fun insertarVisita(v: Visita, participantes: List<Long>): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val id = db.insert("visitas", null, ContentValues().apply {
                put("numero_ot", v.numeroOt); put("id_instalacion", v.idInstalacion); put("fecha", v.fecha)
                put("tipo_visita", v.tipoVisita.name); put("id_ingeniero_cargo", v.idIngenieroCargo)
            })
            participantes.forEach {
                db.insertWithOnConflict("visita_participantes", null, ContentValues().apply { put("id_visita", id); put("id_usuario", it) }, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun cerrarVisita(id: Long, cierre: Long, nombre: String, rut: String, cargo: String): Int =
        writableDatabase.update("visitas", ContentValues().apply {
            put("fecha_hora_cierre", cierre); put("receptor_nombre", nombre); put("receptor_rut", rut); put("receptor_cargo", cargo)
        }, "id = ?", arrayOf(id.toString()))

    // ------------------------------------------------------------------
    //  Protocolos
    // ------------------------------------------------------------------
    fun listarProtocolosDeVisita(idVisita: Long): List<Protocolo> =
        consultar("SELECT * FROM protocolos WHERE id_visita = ?", arrayOf(idVisita.toString()), ::leerProtocolo)
    fun listarProtocolosDeEquipo(idEquipo: Long): List<Protocolo> =
        consultar("SELECT * FROM protocolos WHERE id_equipo = ? ORDER BY fecha_hora_prueba DESC", arrayOf(idEquipo.toString()), ::leerProtocolo)
    fun listarProtocolosPorEstado(estado: EstadoProtocolo): List<Protocolo> =
        consultar("SELECT * FROM protocolos WHERE estado = ? ORDER BY fecha_hora_prueba", arrayOf(estado.name), ::leerProtocolo)
    fun buscarProtocolo(id: Long): Protocolo? = consultar("SELECT * FROM protocolos WHERE id = ?", arrayOf(id.toString()), ::leerProtocolo).firstOrNull()
    fun buscarProtocoloDeVisitaYEquipo(idVisita: Long, idEquipo: Long): Protocolo? =
        consultar("SELECT * FROM protocolos WHERE id_visita = ? AND id_equipo = ?", arrayOf(idVisita.toString(), idEquipo.toString()), ::leerProtocolo).firstOrNull()

    fun insertarProtocolo(p: Protocolo): Long = writableDatabase.insert("protocolos", null, valoresDe(p))
    fun actualizarProtocolo(p: Protocolo): Int = writableDatabase.update("protocolos", valoresDe(p), "id = ?", arrayOf(p.id.toString()))

    private fun valoresDe(p: Protocolo) = ContentValues().apply {
        put("numero", p.numero); put("id_visita", p.idVisita); put("id_equipo", p.idEquipo)
        put("id_usuario_registra", p.idUsuarioRegistra); put("fecha_hora_prueba", p.fechaHoraPrueba)
        put("temperatura_c", p.temperaturaC); put("humedad_pct", p.humedadPct)
        put("descripcion_mantenimiento", p.descripcionMantenimiento); put("estado", p.estado.name)
        put("foto", p.foto); put("latitud", p.latitud); put("longitud", p.longitud)
    }

    fun insertarHistorialEstado(h: HistorialEstado): Long = writableDatabase.insert("historial_estados", null, ContentValues().apply {
        put("id_protocolo", h.idProtocolo); put("estado_anterior", h.estadoAnterior?.name); put("estado_nuevo", h.estadoNuevo.name)
        put("id_usuario", h.idUsuario); put("fecha_hora", h.fechaHora); put("motivo", h.motivo)
    })

    fun listarHistorialEstados(idProtocolo: Long): List<HistorialEstado> =
        consultar("SELECT * FROM historial_estados WHERE id_protocolo = ? ORDER BY fecha_hora", arrayOf(idProtocolo.toString())) { c ->
            HistorialEstado(c.lng("id"), c.lng("id_protocolo"), c.strN("estado_anterior")?.let { EstadoProtocolo.valueOf(it) },
                EstadoProtocolo.valueOf(c.str("estado_nuevo")), c.lng("id_usuario"), c.lng("fecha_hora"), c.strN("motivo"))
        }

    // ------------------------------------------------------------------
    //  Pruebas y mediciones
    // ------------------------------------------------------------------
    fun listarPruebasDeProtocolo(idProtocolo: Long): List<Prueba> =
        consultar("SELECT * FROM pruebas WHERE id_protocolo = ? ORDER BY id", arrayOf(idProtocolo.toString()), ::leerPrueba)

    /** Inserta la prueba y todas sus mediciones juntas: o se guardan las dos cosas o ninguna (transacción). */
    fun insertarPruebaConMediciones(p: Prueba, mediciones: List<Medicion>): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val idPrueba = db.insert("pruebas", null, ContentValues().apply {
                put("id_protocolo", p.idProtocolo); put("tipo_prueba", p.tipoPrueba.name); put("elemento", p.elemento)
                put("momento", p.momento.name); put("realizada", if (p.realizada) 1 else 0); put("motivo_no_realizada", p.motivoNoRealizada)
                put("id_instrumento", p.idInstrumento); put("resultado", p.resultado?.name)
                put("valor_derivado", p.valorDerivado); put("etiqueta_derivada", p.etiquetaDerivada)
            })
            mediciones.forEach {
                db.insert("mediciones", null, ContentValues().apply {
                    put("id_prueba", idPrueba); put("parametro", it.parametro); put("valor", it.valor); put("unidad", it.unidad)
                })
            }
            db.setTransactionSuccessful()
            return idPrueba
        } finally {
            db.endTransaction()
        }
    }

    fun actualizarResultadoPrueba(idPrueba: Long, resultado: ResultadoPrueba): Int =
        writableDatabase.update("pruebas", ContentValues().apply { put("resultado", resultado.name) }, "id = ?", arrayOf(idPrueba.toString()))

    fun listarMedicionesDePrueba(idPrueba: Long): List<Medicion> =
        consultar("SELECT * FROM mediciones WHERE id_prueba = ?", arrayOf(idPrueba.toString())) { c ->
            Medicion(c.lng("id"), c.lng("id_prueba"), c.str("parametro"), c.dbl("valor"), c.str("unidad"))
        }

    // ------------------------------------------------------------------
    //  Lectura de Cursor -> modelo
    // ------------------------------------------------------------------
    private fun <T> consultar(sql: String, args: Array<String>?, mapa: (Cursor) -> T): List<T> {
        val lista = mutableListOf<T>()
        readableDatabase.rawQuery(sql, args).use { c -> while (c.moveToNext()) lista.add(mapa(c)) }
        return lista
    }

    private fun Cursor.col(n: String) = getColumnIndexOrThrow(n)
    private fun Cursor.str(n: String): String = getString(col(n))
    private fun Cursor.strN(n: String): String? = col(n).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.lng(n: String): Long = getLong(col(n))
    private fun Cursor.lngN(n: String): Long? = col(n).let { if (isNull(it)) null else getLong(it) }
    private fun Cursor.dbl(n: String): Double = getDouble(col(n))
    private fun Cursor.dblN(n: String): Double? = col(n).let { if (isNull(it)) null else getDouble(it) }
    private fun Cursor.blobN(n: String): ByteArray? = col(n).let { if (isNull(it)) null else getBlob(it) }

    private fun leerUsuario(c: Cursor) = Usuario(
        c.lng("id"), Rol.valueOf(c.str("rol")), c.str("rut"), c.str("nombre"), c.str("correo"), c.str("telefono"),
        c.lng("activo") == 1L, c.strN("giro"), c.strN("contacto_nombre"), c.strN("contacto_cargo")
    )

    private fun leerInstalacion(c: Cursor) =
        Instalacion(c.lng("id"), c.lngN("id_usuario_cliente"), c.str("nombre"), c.str("comuna"), c.str("observaciones_acceso"))

    private fun leerEquipo(c: Cursor) = Equipo(
        c.lng("id"), c.lng("id_instalacion"), c.str("tag"), TipoEquipo.valueOf(c.str("tipo")), c.str("marca"),
        c.str("numero_serie"), c.str("tensiones"), c.dblN("max_contactos_uohm")
    )

    private fun leerInstrumento(c: Cursor) = Instrumento(
        c.lng("id"), c.str("nombre"), c.str("marca"), c.str("modelo"), c.str("numero_serie"),
        c.lng("fecha_ultima_calibracion"), c.lng("fecha_vencimiento")
    )

    private fun leerVisita(c: Cursor) = Visita(
        c.lng("id"), c.str("numero_ot"), c.lng("id_instalacion"), c.lng("fecha"), TipoVisita.valueOf(c.str("tipo_visita")),
        c.lng("id_ingeniero_cargo"), c.lngN("fecha_hora_cierre"), c.strN("receptor_nombre"), c.strN("receptor_rut"), c.strN("receptor_cargo")
    )

    private fun leerProtocolo(c: Cursor) = Protocolo(
        c.lng("id"), c.str("numero"), c.lng("id_visita"), c.lng("id_equipo"), c.lng("id_usuario_registra"),
        c.lng("fecha_hora_prueba"), c.dblN("temperatura_c"), c.dblN("humedad_pct"), c.strN("descripcion_mantenimiento"),
        EstadoProtocolo.valueOf(c.str("estado")),
        // getBlob sobre un NULL no sirve: por eso blobN() revisa isNull primero (igual que el avatar en MiRegistro)
        c.blobN("foto"), c.dblN("latitud"), c.dblN("longitud")
    )

    private fun leerPrueba(c: Cursor) = Prueba(
        c.lng("id"), c.lng("id_protocolo"), TipoPrueba.valueOf(c.str("tipo_prueba")), c.str("elemento"),
        MomentoPrueba.valueOf(c.str("momento")), c.lng("realizada") == 1L, c.strN("motivo_no_realizada"),
        c.lngN("id_instrumento"), c.strN("resultado")?.let { ResultadoPrueba.valueOf(it) },
        c.dblN("valor_derivado"), c.strN("etiqueta_derivada")
    )
}
