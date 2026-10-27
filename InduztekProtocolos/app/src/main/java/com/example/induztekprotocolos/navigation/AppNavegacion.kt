package com.example.induztekprotocolos.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.ui.screens.ClienteScreen
import com.example.induztekprotocolos.ui.screens.HistorialScreen
import com.example.induztekprotocolos.ui.screens.InicioScreen
import com.example.induztekprotocolos.ui.screens.InstrumentosScreen
import com.example.induztekprotocolos.ui.screens.LoginScreen
import com.example.induztekprotocolos.ui.screens.NuevaVisitaScreen
import com.example.induztekprotocolos.ui.screens.ProtocoloScreen
import com.example.induztekprotocolos.ui.screens.RevisionScreen
import com.example.induztekprotocolos.ui.screens.VisitaDetalleScreen
import com.example.induztekprotocolos.ui.screens.VisitasScreen
import com.example.induztekprotocolos.viewmodel.InduztekViewModel

private const val DURACION = 320

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    // ViewModel compartido por todas las pantallas (igual que UsuariosViewModel en MiRegistro).
    val vm: InduztekViewModel = viewModel()
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val visitas by vm.visitas.collectAsStateWithLifecycle()
    val instrumentos by vm.instrumentos.collectAsStateWithLifecycle()
    val porRevisar by vm.porRevisar.collectAsStateWithLifecycle()
    val detalle by vm.detalle.collectAsStateWithLifecycle()

    // Si quedó una sesión guardada (SharedPreferences) se entra directo al inicio.
    val destinoInicial = remember { if (vm.usuarioActual.value != null) Ruta.Inicio.ruta else Ruta.Login.ruta }

    // ANIMACIÓN (IE 2.2.2): transición entre pantallas = deslizar + fundido.
    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        enterTransition = { slideInHorizontally(tween(DURACION)) { it } + fadeIn(tween(DURACION)) },
        exitTransition = { slideOutHorizontally(tween(DURACION)) { -it / 3 } + fadeOut(tween(DURACION)) },
        popEnterTransition = { slideInHorizontally(tween(DURACION)) { -it / 3 } + fadeIn(tween(DURACION)) },
        popExitTransition = { slideOutHorizontally(tween(DURACION)) { it } + fadeOut(tween(DURACION)) }
    ) {
        composable(Ruta.Login.ruta) {
            LoginScreen(
                cuentas = vm.cuentas,
                onEntrar = { id ->
                    vm.iniciarSesion(id)
                    navController.navigate(Ruta.Inicio.ruta) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
                }
            )
        }

        composable(Ruta.Inicio.ruta) {
            val u = usuario ?: return@composable
            LaunchedEffect(Unit) { vm.recargar() }
            InicioScreen(
                usuario = u,
                pendientesRevision = porRevisar.size,
                onVisitas = { navController.navigate(Ruta.Visitas.ruta) },
                onRevision = { navController.navigate(Ruta.Revision.ruta) },
                onInstrumentos = { navController.navigate(Ruta.Instrumentos.ruta) },
                onMisEquipos = { navController.navigate(Ruta.MisEquipos.ruta) },
                onCerrarSesion = {
                    vm.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(Ruta.Inicio.ruta) { inclusive = true } }
                }
            )
        }

        composable(Ruta.Visitas.ruta) {
            LaunchedEffect(Unit) { vm.recargar() }
            VisitasScreen(
                visitas = visitas,
                puedeCrear = vm.puede(Permiso.REGISTRAR_PROTOCOLO),
                onVolver = { navController.navigateUp() },
                onNueva = { navController.navigate(Ruta.NuevaVisita.ruta) },
                onAbrir = { id -> navController.navigate(Ruta.Visita.crear(id)) }
            )
        }

        composable(Ruta.NuevaVisita.ruta) {
            val u = usuario ?: return@composable
            NuevaVisitaScreen(
                usuario = u,
                instalaciones = remember { vm.instalaciones() },
                ingenieros = remember { vm.ingenieros() },
                tecnicos = remember { vm.tecnicos() },
                onVolver = { navController.navigateUp() },
                onCrear = vm::crearVisita,
                onCreada = { id ->
                    navController.navigate(Ruta.Visita.crear(id)) { popUpTo(Ruta.NuevaVisita.ruta) { inclusive = true } }
                }
            )
        }

        composable(
            route = Ruta.Visita.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            LaunchedEffect(id) { vm.cargarDetalle(id) }
            VisitaDetalleScreen(
                detalle = detalle?.takeIf { it.visita.id == id },
                puedeGestionarEquipos = vm.puede(Permiso.GESTIONAR_CLIENTES_EQUIPOS),
                puedeCerrar = vm.puede(Permiso.CERRAR_VISITA),
                onVolver = { navController.navigateUp() },
                onProtocolo = { idEquipo ->
                    val r = vm.abrirProtocolo(id, idEquipo)
                    if (r.ok && r.valor != null) navController.navigate(Ruta.Protocolo.crear(r.valor))
                    r.error
                },
                onHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },
                onRegistrarEquipo = vm::registrarEquipo,
                onCerrarVisita = { nombre, rut, cargo -> vm.cerrarVisita(id, nombre, rut, cargo) }
            )
        }

        composable(
            route = Ruta.Protocolo.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val u = usuario ?: return@composable
            val id = entrada.arguments?.getLong("id") ?: 0L
            ProtocoloScreen(
                idProtocolo = id,
                usuario = u,
                onVolver = { navController.navigateUp() },
                onHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) }
            )
        }

        composable(
            route = Ruta.Historial.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val esCliente = usuario?.rol == com.example.induztekprotocolos.domain.Rol.CLIENTE
            HistorialScreen(
                nombreEquipo = remember(idEquipo) { vm.nombreEquipo(idEquipo) },
                protocolos = remember(idEquipo, usuario) { vm.historialDeEquipo(idEquipo) },
                onVolver = { navController.navigateUp() },
                onAbrirProtocolo = if (esCliente) null else ({ id: Long -> navController.navigate(Ruta.Protocolo.crear(id)) })
            )
        }

        composable(Ruta.Revision.ruta) {
            LaunchedEffect(Unit) { vm.recargar() }
            RevisionScreen(
                pendientes = porRevisar,
                onVolver = { navController.navigateUp() },
                onAbrir = { id -> navController.navigate(Ruta.Protocolo.crear(id)) }
            )
        }

        composable(Ruta.Instrumentos.ruta) {
            InstrumentosScreen(
                instrumentos = instrumentos,
                puedeGestionar = vm.puede(Permiso.GESTIONAR_INSTRUMENTOS),
                onVolver = { navController.navigateUp() },
                onRegistrar = vm::registrarInstrumento
            )
        }

        composable(Ruta.MisEquipos.ruta) {
            val u = usuario ?: return@composable
            ClienteScreen(
                nombreEmpresa = u.nombre,
                instalaciones = remember(u.id) { vm.instalacionesDelCliente() },
                equiposDe = vm::equiposDe,
                onVolver = { navController.navigateUp() },
                onHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) }
            )
        }
    }
}
