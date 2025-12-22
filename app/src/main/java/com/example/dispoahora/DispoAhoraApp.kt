package com.example.dispoahora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dispoahora.contacts.ContactsScreen
import com.example.dispoahora.login.AuthState
import com.example.dispoahora.login.AuthViewModel
import com.example.dispoahora.login.LoginScreen
import com.example.dispoahora.login.ProfileScreen

// 1. Definimos las rutas (Nombres de las pantallas)
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Contacts: Screen("contacts")
}

@Composable
fun DispoAhoraApp(authViewModel: AuthViewModel) {
    // 2. Inicializamos el controlador de navegación
    val navController = rememberNavController()

    // 3. Obtenemos la ruta actual para saber qué barras mostrar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 4. Observamos el estado de autenticación
    val authState by authViewModel.authState.collectAsState()

    // 5. Decidimos dónde empieza la app (Login o Home)
    // Nota: Esto es una lógica simple. Si authState es Loading, podrías mostrar un spinner.
    val startDestination = if (authState is AuthState.SignedIn) Screen.Home.route else Screen.Login.route

    Scaffold(
        containerColor = Color.Transparent,

        bottomBar = {
            if (currentRoute == Screen.Home.route) {
                CustomBottomBar(
                    onContactsClick = { navController.navigate(Screen.Contacts.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            composable(Screen.Login.route) {
                LoginScreen(authViewModel)
            }

            composable(Screen.Contacts.route) {
                ContactsScreen()
            }

            composable(Screen.Home.route) {
                val user = (authState as? AuthState.SignedIn)?.userName ?: "Usuario"
                val avatar = (authState as? AuthState.SignedIn)?.avatarUrl
                DispoAhoraScreen(
                    username = user,
                    avatar,
                    onOpenProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(Screen.Profile.route) {
                val user = (authState as? AuthState.SignedIn)?.userName ?: "Usuario"
                val email = "usuario@ejemplo.com"

                ProfileScreen(
                    username = user,
                    email = email,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}