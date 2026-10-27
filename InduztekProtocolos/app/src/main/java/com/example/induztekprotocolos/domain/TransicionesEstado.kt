package com.example.induztekprotocolos.domain

import com.example.induztekprotocolos.domain.EstadoProtocolo.*

/** RN-04, tal cual la tabla Desde/Hacia/Quién. Función pura: no depende de Android ni de la UI. */
object TransicionesEstado {
    fun permitida(desde: EstadoProtocolo, hacia: EstadoProtocolo, rol: Rol): Boolean = when {
        hacia == FORMALIZADO -> desde == REGISTRADO_TERRENO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        hacia == OBSERVADO -> desde == REGISTRADO_TERRENO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        hacia == REGISTRADO_TERRENO && desde == OBSERVADO -> rol in setOf(Rol.TECNICO, Rol.INGENIERO)
        hacia == REGISTRADO_TERRENO && desde == BORRADOR -> rol in setOf(Rol.TECNICO, Rol.INGENIERO)
        hacia == ANULADO -> desde != FORMALIZADO && desde != ANULADO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        else -> false
    }

    /** Un protocolo Formalizado nunca se puede modificar ni anular (RN-04). */
    fun esFinal(estado: EstadoProtocolo) = estado == FORMALIZADO

    /** Solo se puede editar mientras está en Borrador u Observado. */
    fun editable(estado: EstadoProtocolo) = estado == BORRADOR || estado == OBSERVADO

    /** El motivo es obligatorio al observar o anular. */
    fun requiereMotivo(hacia: EstadoProtocolo) = hacia == OBSERVADO || hacia == ANULADO
}
