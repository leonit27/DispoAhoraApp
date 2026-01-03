package com.example.dispoahora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dispoahora.contacts.ContactsScreen
import com.example.dispoahora.login.AuthState
import com.example.dispoahora.login.AuthViewModel
import com.example.dispoahora.login.LoginScreen
import com.example.dispoahora.login.ProfileScreen
import com.example.dispoahora.utils.CustomBottomBar

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Contacts: Screen("contacts")
}

@Composable
fun DispoAhoraApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authState by authViewModel.authState.collectAsState()

    val startDestination = if (authState is AuthState.SignedIn) Screen.Home.route else Screen.Login.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color.White,
                    contentColor = Color.DarkGray,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(12.dp)
                ) { Text(data.visuals.message) }
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
                CustomBottomBar(
                    currentRoute = currentRoute,
                    onHomeClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onContactsClick = { navController.navigate(Screen.Contacts.route) },
                    onSettingsClick = { navController.navigate(Screen.Profile.route) }
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
                    snackbarHostState = snackbarHostState,
                    coroutineScope = scope,
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