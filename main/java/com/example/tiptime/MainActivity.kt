/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

// Actividad principal de la aplicación, donde se configura el diseño usando Jetpack Compose.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Habilita el diseño sin bordes.
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme { // Aplica el tema personalizado de la aplicación.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout() // Llama al diseño principal de la pantalla.
                }
            }
        }
    }
}

// Diseño principal de la aplicación.
@Composable
fun TipTimeLayout() {
    // Variables de estado para almacenar las entradas del usuario y la configuración.
    var amountInput by remember { mutableStateOf("") }
    var tipInput by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) }

    // Convierte las entradas de texto a valores numéricos (o 0.0 si no son válidos).
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0

    // Calcula la propina según la entrada del usuario.
    val tip = calculateTip(amount, tipPercent, roundUp)

    // Estructura de diseño principal.
    Column(
        modifier = Modifier
            .statusBarsPadding() // Ajusta el diseño debajo de la barra de estado.
            .padding(horizontal = 40.dp) // Agrega margen horizontal.
            .verticalScroll(rememberScrollState()) // Permite el desplazamiento vertical.
            .safeDrawingPadding(), // Asegura un diseño seguro en los bordes.
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título principal.
        Text(
            text = stringResource(R.string.calculate_tip), // Texto de recurso.
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp) // Márgenes superior e inferior.
                .align(alignment = Alignment.Start) // Alineación del texto a la izquierda.
        )

        // Campo de entrada para el monto de la factura.
        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number, // Tipo de teclado numérico.
                imeAction = ImeAction.Next // Acción del teclado: ir al siguiente campo.
            ),
            value = amountInput,
            onValueChanged = { amountInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp) // Espaciado inferior.
                .fillMaxWidth(), // Ocupa todo el ancho disponible.
        )

        // Campo de entrada para el porcentaje de propina.
        EditNumberField(
            label = R.string.how_was_the_service,
            leadingIcon = R.drawable.percent,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done // Acción del teclado: finalizar.
            ),
            value = tipInput,
            onValueChanged = { tipInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        )

        // Fila para configurar si la propina debe redondearse.
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Muestra el monto de la propina calculada.
        Text(
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall // Estilo de texto.
        )

        // Espaciador adicional para centrar el contenido.
        Spacer(modifier = Modifier.height(150.dp))
    }
}

// Composable para un campo de entrada con etiqueta e icono.
@Composable
fun EditNumberField(
    @StringRes label: Int, // Recurso de texto para la etiqueta.
    @DrawableRes leadingIcon: Int, // Recurso de imagen para el icono.
    keyboardOptions: KeyboardOptions, // Configuración del teclado.
    value: String, // Valor del campo.
    onValueChanged: (String) -> Unit, // Callback para manejar cambios en el valor.
    modifier: Modifier = Modifier // Modificadores opcionales.
) {
    TextField(
        value = value,
        singleLine = true, // Restringe el campo a una sola línea.
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) }, // Icono principal.
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) }, // Etiqueta del campo.
        keyboardOptions = keyboardOptions
    )
}

// Fila para alternar el redondeo de la propina.
@Composable
fun RoundTheTipRow(
    roundUp: Boolean, // Estado actual del interruptor.
    onRoundUpChanged: (Boolean) -> Unit, // Callback para manejar cambios.
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto que describe la funcionalidad del interruptor.
        Text(text = stringResource(R.string.round_up_tip))
        // Interruptor para activar/desactivar el redondeo.
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End), // Alineación a la derecha.
            checked = roundUp,
            onCheckedChange = onRoundUpChanged
        )
    }
}

// Función para calcular la propina, opcionalmente redondeada.
private fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean): String {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip) // Redondea hacia arriba.
    }
    // Formatea la propina como moneda local.
    return NumberFormat.getCurrencyInstance().format(tip)
}

// Vista previa del diseño para herramientas de diseño en Android Studio.
@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}
