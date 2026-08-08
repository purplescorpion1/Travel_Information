package com.travelplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelplanner.ui.screens.SearchScreen
import com.travelplanner.ui.screens.ServiceDetailScreen
import com.travelplanner.ui.screens.SettingsScreen
import com.travelplanner.ui.screens.StationBoardScreen
import com.travelplanner.viewmodel.MainViewModel
import com.travelplanner.viewmodel.ServiceDetailViewModel
import com.travelplanner.viewmodel.SettingsViewModel
import com.travelplanner.viewmodel.StationBoardViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val boardViewModel: StationBoardViewModel by viewModels()
    private val serviceDetailViewModel: ServiceDetailViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Modern premium Material 3 Light Color Scheme
            val lightColorScheme = lightColorScheme(
                primary = Color(0xFF005A9C), // Train Blue
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1E4FF),
                onPrimaryContainer = Color(0xFF001D36),
                secondary = Color(0xFF535F70),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFD7E3F7),
                onSecondaryContainer = Color(0xFF101C2B),
                background = Color(0xFFF8F9FF),
                surface = Color.White,
                onBackground = Color(0xFF1A1C1E),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFDFE2EB),
                onSurfaceVariant = Color(0xFF43474E),
                outline = Color(0xFF73777F),
                error = Color(0xFFBA1A1A),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002)
            )

            MaterialTheme(colorScheme = lightColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "search"
                    ) {
                        composable("search") {
                            SearchScreen(
                                viewModel = mainViewModel,
                                onNavigateToStationBoard = { crs, name, dateTime ->
                                    val route = if (dateTime != null) {
                                        "station_board/$crs/$name?dateTime=$dateTime"
                                    } else {
                                        "station_board/$crs/$name"
                                    }
                                    navController.navigate(route)
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable(
                            route = "station_board/{crs}/{stationName}?dateTime={dateTime}",
                            arguments = listOf(
                                navArgument("crs") { type = NavType.StringType },
                                navArgument("stationName") { type = NavType.StringType },
                                navArgument("dateTime") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val crs = backStackEntry.arguments?.getString("crs") ?: ""
                            val name = backStackEntry.arguments?.getString("stationName") ?: ""
                            val dateTime = backStackEntry.arguments?.getString("dateTime")

                            StationBoardScreen(
                                crs = crs,
                                stationName = name,
                                formattedDateTime = dateTime,
                                boardViewModel = boardViewModel,
                                mainViewModel = mainViewModel,
                                onNavigateToServiceDetail = { uniqueIdentity ->
                                    navController.navigate("service_detail/$uniqueIdentity")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "service_detail/{uniqueIdentity}",
                            arguments = listOf(
                                navArgument("uniqueIdentity") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val uid = backStackEntry.arguments?.getString("uniqueIdentity") ?: ""
                            ServiceDetailScreen(
                                uniqueIdentity = uid,
                                viewModel = serviceDetailViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
