package com.example.induztekprotocolos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * ANIMACIÓN (IE 2.2.2): cada elemento de una lista aparece con un fundido + deslizamiento desde abajo,
 * uno después del otro (retraso según su posición). "visible" cambia a true la primera vez que se dibuja.
 */
@Composable
fun AparecerAnimado(indice: Int, contenido: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((indice.coerceAtMost(8) * 60).toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { it / 3 }
    ) {
        contenido()
    }
}
