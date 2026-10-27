package com.example.induztekprotocolos.domain

enum class Permiso {
    GESTIONAR_CLIENTES_EQUIPOS, REGISTRAR_PROTOCOLO, CERRAR_VISITA,
    FORMALIZAR_OBSERVAR, ANULAR_PROTOCOLO, GESTIONAR_INSTRUMENTOS,
    GESTIONAR_PERSONAL, OTORGAR_CUENTAS_CLIENTE, CONSULTAR_HISTORIAL,
    CONSULTAR_PROPIA_EMPRESA
}

/**
 * Anexo 7 (tabla de permisos) como datos. Ojo: no confundir con permisos/Permisos.kt,
 * que son los permisos de ANDROID (cámara, galería, ubicación).
 */
object PermisosRol {
    private val matriz: Map<Rol, Set<Permiso>> = mapOf(
        Rol.TECNICO to setOf(Permiso.GESTIONAR_CLIENTES_EQUIPOS, Permiso.REGISTRAR_PROTOCOLO, Permiso.CERRAR_VISITA, Permiso.CONSULTAR_HISTORIAL),
        Rol.INGENIERO to setOf(Permiso.GESTIONAR_CLIENTES_EQUIPOS, Permiso.REGISTRAR_PROTOCOLO, Permiso.CERRAR_VISITA, Permiso.FORMALIZAR_OBSERVAR, Permiso.ANULAR_PROTOCOLO, Permiso.CONSULTAR_HISTORIAL),
        Rol.JEFE_TECNICO to setOf(Permiso.GESTIONAR_CLIENTES_EQUIPOS, Permiso.REGISTRAR_PROTOCOLO, Permiso.FORMALIZAR_OBSERVAR, Permiso.ANULAR_PROTOCOLO, Permiso.GESTIONAR_INSTRUMENTOS, Permiso.GESTIONAR_PERSONAL, Permiso.OTORGAR_CUENTAS_CLIENTE, Permiso.CONSULTAR_HISTORIAL),
        Rol.CLIENTE to setOf(Permiso.CONSULTAR_PROPIA_EMPRESA)
    )

    fun tiene(rol: Rol, permiso: Permiso): Boolean = matriz[rol]?.contains(permiso) == true
}
