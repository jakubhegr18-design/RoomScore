package com.roomanalyzer.roomscore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roomanalyzer.roomscore.navigation.Screen
import com.roomanalyzer.roomscore.ui.screens.CameraScreen
import com.roomanalyzer.roomscore.ui.screens.HomeScreen
import com.roomanalyzer.roomscore.ui.screens.ResultScreen
import com.roomanalyzer.roomscore.ui.screens.SettingsScreen
import com.roomanalyzer.roomscore.viewmodel.RoomViewModel

@Composable
fun RoomScoreApp(viewModel: RoomViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartScan = {
                    viewModel.reset()
                    navController.navigate(Screen.Camera.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                photoCount = viewModel.photoCount,
                maxPhotos = RoomViewModel.MAX_PHOTOS,
                capturedPhotos = viewModel.capturedPhotos,
                isProcessing = viewModel.isAnalyzing,
                onPhotoTaken = { bitmap ->
                    viewModel.addPhoto(bitmap)
                },
                onFinishCapture = {
                    viewModel.finishCapture()
                },
                onClose = {
                    viewModel.reset()
                    navController.popBackStack()
                }
            )

            LaunchedEffect(viewModel.isAnalyzing, viewModel.analysis) {
                if (!viewModel.isAnalyzing && viewModel.analysis != null) {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            }
        }

        composable(Screen.Result.route) {
            val analysis = viewModel.analysis
            if (analysis != null) {
                ResultScreen(
                    analysis = analysis,
                    onScanAgain = {
                        viewModel.reset()
                        navController.navigate(Screen.Camera.route)
                    }
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                config = viewModel.haConfig,
                onConfigChanged = { config ->
                    viewModel.updateHaConfig(config)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
