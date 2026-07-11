package com.shkurta.medication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shkurta.medication.ui.add.AddMedicationScreen
import com.shkurta.medication.ui.details.MedicationDetailsScreen
import com.shkurta.medication.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val MEDICATION_DETAILS = "medication_details"
    const val MEDICATION_DETAILS_ROUTE = "$MEDICATION_DETAILS/{medicationId}"

    fun medicationDetails(id: Long) = "$MEDICATION_DETAILS/$id"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddClick = { navController.navigate(Routes.ADD) },
                onHistoryClick = { medicationId ->
                    navController.navigate(Routes.medicationDetails(medicationId))
                }
            )
        }
        composable(Routes.ADD) {
            AddMedicationScreen(onDone = { navController.popBackStack() })
        }
        composable(
            route = Routes.MEDICATION_DETAILS_ROUTE,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) {
            MedicationDetailsScreen(onBack = { navController.popBackStack() })
        }
    }
}
