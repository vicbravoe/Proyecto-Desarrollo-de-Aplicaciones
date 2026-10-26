package com.example.induztekprotocolos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.induztekprotocolos.ui.equipos.EquiposListScreen
import com.example.induztekprotocolos.ui.historial.HistorialScreen
import com.example.induztekprotocolos.ui.protocolo.ProtocoloFormScreen

@Composable
fun InduztekNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Equipos.route
    ) {
        composable(Rutas.Equipos.route) {
            EquiposListScreen(
                onNavigateToForm = { equipoId ->
                    navController.navigate(Rutas.ProtocoloForm.crearRuta(equipoId))
                },
                onNavigateToHistorial = {
                    navController.navigate(Rutas.Historial.route)
                }
            )
        }

        composable(
            route = Rutas.ProtocoloForm.route,
            arguments = listOf(navArgument("equipoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val equipoId = backStackEntry.arguments?.getLong("equipoId") ?: 0L
            ProtocoloFormScreen(
                equipoId = equipoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Rutas.Historial.route) {
            HistorialScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}