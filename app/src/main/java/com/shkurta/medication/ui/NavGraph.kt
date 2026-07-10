package com.shkurta.medication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shkurta.medication.ui.add.AddMedicationScreen
import com.shkurta.medication.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onAddClick = { navController.navigate(Routes.ADD) })
        }
        composable(Routes.ADD) {
            AddMedicationScreen(onDone = { navController.popBackStack() })
        }
    }
}
