package cl.induztek.protocolos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * Igual que en MiRegistro: estado elevado (state hoisting). El campo no
 * guarda su propio texto, solo muestra "valor" y avisa cambios por
 * "onValorCambia" — quien decide qué hacer con eso es el ViewModel.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    soloLectura: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        readOnly = soloLectura,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = ImeAction.Next)
    )
}