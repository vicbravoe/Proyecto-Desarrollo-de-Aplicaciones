package com.example.induztekprotocolos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.induztekprotocolos.R
import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.FormPrueba
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Visita
import com.example.induztekprotocolos.ui.Formato
import com.example.induztekprotocolos.ui.components.BotonPrincipal
import com.example.induztekprotocolos.ui.components.BotonSecundario
import com.example.induztekprotocolos.ui.components.CampoTexto
import com.example.induztekprotocolos.ui.components.ChipResultado
import com.example.induztekprotocolos.ui.components.InterruptorConTexto
import com.example.induztekprotocolos.ui.components.SelectorDesplegable
import com.example.induztekprotocolos.ui.components.TarjetaSeccion
import com.example.induztekprotocolos.ui.theme.Dimens
import com.example.induztekprotocolos.viewmodel.ProtocoloViewModel

/**
 * Formulario para registrar una prueba (RF-04 a RF-06). Los parámetros que se piden dependen del
 * tipo de equipo y de la prueba (CatalogoPruebas, RN-05). Aquí NO se valida: cada cambio se avisa al
 * ViewModel y los mensajes de error vuelven en "form".
 */
@Composable
fun FormularioPrueba(
    form: FormPrueba,
    equipo: Equipo,
    visita: Visita?,
    instrumentos: List<Instrumento>,
    vm: ProtocoloViewModel
) {
    // ANIMACIÓN: el formulario se despliega y se pliega suavemente al abrir/cerrar.
    AnimatedVisibility(
        visible = form.visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        TarjetaSeccion(titulo = "Nueva prueba") {
            val pruebas = CatalogoPruebas.pruebasDe(equipo.tipo)
            SelectorDesplegable(
                etiqueta = "Prueba",
                opciones = pruebas,
                seleccionada = form.tipo,
                textoDe = { it.etiqueta },
                onSeleccionar = vm::onTipoPrueba,
                error = form.errorTipo
            )
            val tipo = form.tipo
            if (tipo != null) {
                SelectorDesplegable(
                    etiqueta = "Elemento (devanado, fase o polo)",
                    opciones = CatalogoPruebas.elementosSugeridos(tipo, equipo.tipo),
                    seleccionada = form.elemento.ifBlank { null },
                    textoDe = { it },
                    onSeleccionar = vm::onElemento,
                    error = form.errorElemento
                )
                if (visita?.tipoVisita == TipoVisita.PRUEBAS_Y_MANTENIMIENTO) {
                    SelectorDesplegable(
                        etiqueta = "Momento",
                        opciones = listOf(MomentoPrueba.PREVIA, MomentoPrueba.POSTERIOR),
                        seleccionada = form.momento,
                        textoDe = { it.etiqueta },
                        onSeleccionar = vm::onMomento
                    )
                }
                InterruptorConTexto(texto = "Prueba realizada", activado = form.realizada, onCambio = vm::onRealizada)

                if (form.realizada) {
                    SelectorDesplegable(
                        etiqueta = "Instrumento utilizado",
                        opciones = instrumentos,
                        seleccionada = instrumentos.firstOrNull { it.id == form.idInstrumento },
                        textoDe = { i -> if (i.fechaVencimiento < System.currentTimeMillis()) "${i.nombre} (VENCIDO)" else i.nombre },
                        onSeleccionar = { vm.onInstrumento(it.id) },
                        error = form.errorInstrumento
                    )
                    CatalogoPruebas.parametrosDe(tipo, equipo.tipo).forEach { parametro ->
                        CampoTexto(
                            valor = form.valores[parametro.codigo].orEmpty(),
                            onValorCambia = { vm.onValor(parametro.codigo, it) },
                            etiqueta = "${parametro.etiqueta} (${parametro.unidad})",
                            icono = R.drawable.ic_editar,
                            error = form.erroresValores[parametro.codigo],
                            tipoTeclado = KeyboardType.Decimal
                        )
                    }
                    if (CatalogoPruebas.esManual(tipo)) {
                        Text("Esta prueba no tiene criterio automático: el ingeniero define el resultado.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        val estimado = form.estimado
                        if (estimado?.resultado != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                Text("Resultado calculado:", style = MaterialTheme.typography.bodyMedium)
                                ChipResultado(estimado.resultado)
                            }
                            if (estimado.valorDerivado != null) {
                                Text("${estimado.etiquetaDerivada ?: "Valor"}: ${Formato.numero(estimado.valorDerivado)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    CampoTexto(
                        valor = form.motivo,
                        onValorCambia = vm::onMotivo,
                        etiqueta = "Motivo por el que no se realizó",
                        icono = R.drawable.ic_editar,
                        error = form.errorMotivo
                    )
                }
            }
            form.errorGeneral?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            BotonPrincipal(texto = "Guardar prueba", icono = R.drawable.ic_check, onClick = vm::guardarPrueba, modifier = Modifier.fillMaxWidth())
            BotonSecundario(texto = "Cancelar", onClick = vm::cerrarFormPrueba, modifier = Modifier.fillMaxWidth())
        }
    }
}
