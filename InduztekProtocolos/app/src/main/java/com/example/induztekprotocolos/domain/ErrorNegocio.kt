package com.example.induztekprotocolos.domain

/** Cualquier violación de una regla de negocio (RN-xx) se comunica con esto, nunca con una excepción genérica. */
class ErrorNegocio(mensaje: String) : Exception(mensaje)
