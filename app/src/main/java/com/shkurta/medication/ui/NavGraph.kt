package com.shkurta.medication.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shkurta.medication.ui.add.AddMedicationScreen
import com.shkurta.medication.ui.details.MedicationDetailsScreen
import com.shkurta.medication.ui.edit.EditMedicationScreen
import com.shkurta.medication.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val MEDICATION_DETAILS = "medication_details"
    const val MEDICATION_DETAILS_ROUTE = "$MEDICATION_DETAILS/{medicationId}"
    const val EDIT_MEDICATION = "edit_medication"
    const val EDIT_MEDICATION_ROUTE = "$EDIT_MEDICATION/{medicationId}"

    fun medicationDetails(id: Long) = "$MEDICATION_DETAILS/$id"
    fun editMedication(id: Long) = "$EDIT_MEDICATION/$id"
}

private const val TRANSITION_DURATION_MS = 350
private const val SLIDE_FRACTION = 6

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(TRANSITION_DURATION_MS),
                initialOffsetX = { it / SLIDE_FRACTION }
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(TRANSITION_DURATION_MS),
                targetOffsetX = { -it / SLIDE_FRACTION }
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(TRANSITION_DURATION_MS),
                initialOffsetX = { -it / SLIDE_FRACTION }
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(TRANSITION_DURATION_MS),
                targetOffsetX = { it / SLIDE_FRACTION }
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddClick = { navController.navigate(Routes.ADD) },
                onHistoryClick = { medicationId ->
                    navController.navigate(Routes.medicationDetails(medicationId))
                },
                onEditClick = { medicationId ->
                    navController.navigate(Routes.editMedication(medicationId))
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
        composable(
            route = Routes.EDIT_MEDICATION_ROUTE,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) {
            EditMedicationScreen(onDone = { navController.popBackStack() })
        }
    }
}