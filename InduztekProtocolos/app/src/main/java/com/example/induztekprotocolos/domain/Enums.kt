package com.example.induztekprotocolos.domain

/** Anexo 2 / Anexo 7: los 4 perfiles del caso. */
enum class Rol(val etiqueta: String) {
    TECNICO("Técnico de terreno"),
    INGENIERO("Ingeniero de pruebas"),
    JEFE_TECNICO("Jefe técnico"),
    CLIENTE("Usuario cliente")
}

enum class ClasificacionEquipo { PRIMARIO, SECUNDARIO }

/** RN-05 / Aclaración 1: los 5 tipos de equipo de esta versión, ya clasificados. */
enum class TipoEquipo(val etiqueta: String, val clasificacion: ClasificacionEquipo) {
    TRANSFORMADOR("Transformador", ClasificacionEquipo.PRIMARIO),
    INTERRUPTOR("Interruptor", ClasificacionEquipo.PRIMARIO),
    CELDA_MT("Celda de media tensión", ClasificacionEquipo.PRIMARIO),
    CABLE_POTENCIA("Cable de poder", ClasificacionEquipo.PRIMARIO),
    RELE_PROTECCION("Relé de protección", ClasificacionEquipo.SECUNDARIO)
}

/** RN-05: las 5 pruebas del catálogo. */
enum class TipoPrueba(val etiqueta: String) {
    AISLACION("Resistencia de aislación"),
    TTR("Relación de transformación (TTR)"),
    DEVANADOS("Resistencia de devanados"),
    CONTACTOS("Resistencia de contactos"),
    OPERACION_RELE("Operación de relé")
}

enum class ResultadoPrueba(val etiqueta: String) {
    APROBADO("Aprobado"), OBSERVADO("Observado"), RECHAZADO("Rechazado")
}

/** Aclaración 4: previa y posterior a un mantenimiento; para el resultado vale la posterior. */
enum class MomentoPrueba(val etiqueta: String) {
    UNICA("Única"), PREVIA("Previa al mantenimiento"), POSTERIOR("Posterior al mantenimiento")
}

enum class TipoVisita(val etiqueta: String) {
    SOLO_PRUEBAS("Solo pruebas"), PRUEBAS_Y_MANTENIMIENTO("Pruebas y mantenimiento")
}

/** RN-04: los 5 estados reales del protocolo. */
enum class EstadoProtocolo(val etiqueta: String) {
    BORRADOR("Borrador"),
    REGISTRADO_TERRENO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado")
}
