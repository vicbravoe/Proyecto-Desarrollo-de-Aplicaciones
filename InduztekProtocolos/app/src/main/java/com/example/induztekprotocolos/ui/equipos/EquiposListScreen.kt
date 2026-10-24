package cl.induztek.protocolos.ui.equipos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.induztek.protocolos.ui.components.TarjetaEquipo
import cl.induztek.protocolos.ui.theme.Dimens

@Composable
fun EquiposListScreen(
    viewModel: EquiposViewModel,
    onEquipoSeleccionado: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.equipos.isEmpty() -> Text(
                "No hay equipos registrados todavía",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                itemsIndexed(uiState.equipos, key = { _, equipo -> equipo.id }) { index, equipo ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        TarjetaEquipo(equipo = equipo, onClick = { onEquipoSeleccionado(equipo.id) })
                    }
                }
            }
        }
    }
}