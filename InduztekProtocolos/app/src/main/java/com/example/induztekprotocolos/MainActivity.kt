package com.example.induztekprotocolos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.induztekprotocolos.navigation.AppNavegacion
import com.example.induztekprotocolos.ui.theme.InduztekTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InduztekTheme {
                AppNavegacion()
            }
        }
    }
}
