package cl.induztek.protocolos.data.local

import androidx.room.TypeConverter

/** Room guarda columnas primitivas; los enums se convierten a/desde su nombre como String. */
class Converters {
    @TypeConverter
    fun tipoEquipoDesde(valor: String) = TipoEquipo.valueOf(valor)
    @TypeConverter
    fun tipoEquipoHacia(valor: TipoEquipo) = valor.name

    @TypeConverter
    fun resultadoDesde(valor: String) = ResultadoPrueba.valueOf(valor)
    @TypeConverter
    fun resultadoHacia(valor: ResultadoPrueba) = valor.name

    @TypeConverter
    fun estadoDesde(valor: String) = EstadoProtocolo.valueOf(valor)
    @TypeConverter
    fun estadoHacia(valor: EstadoProtocolo) = valor.name
}