package cl.induztek.protocolos.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.induztek.protocolos.ui.theme.Dimens

@Composable
fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = habilitado, modifier = modifier.height(Dimens.alturaBoton)) {
        Text(texto)
    }
}
