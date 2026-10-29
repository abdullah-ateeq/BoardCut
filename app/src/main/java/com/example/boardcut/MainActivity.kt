package com.example.boardcut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardcut.ui.navigation.Screen
import com.example.boardcut.ui.screens.AboutScreen
import com.example.boardcut.ui.screens.CalculatorScreen
import com.example.boardcut.ui.screens.HistoryScreen
import com.example.boardcut.ui.screens.ResultsScreen
import com.example.boardcut.ui.screens.SettingsScreen
import com.example.boardcut.ui.screens.SplashScreen
import com.example.boardcut.ui.theme.BoardCutTheme
import com.example.boardcut.ui.viewmodel.BoardCutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BoardCutViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            BoardCutTheme(themeMode = themeMode) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: BoardCutViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val optimizationResult by viewModel.optimizationResult.collectAsState()

    // Handle back button: if not on Calculator screen, go to Calculator first
    BackHandler(enabled = currentScreen != Screen.Calculator) {
        viewModel.navigateTo(Screen.Calculator)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = {
                            if (screen == Screen.Results && optimizationResult != null && optimizationResult!!.boards.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("${optimizationResult!!.boards.size}")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                Icon(imageVector = screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding),
            label = "screen_crossfade"
        ) { screen ->
            when (screen) {
                Screen.Calculator -> CalculatorScreen(viewModel = viewModel)
                Screen.Results -> ResultsScreen(viewModel = viewModel)
                Screen.History -> HistoryScreen(viewModel = viewModel)
                Screen.Settings -> SettingsScreen(viewModel = viewModel)
                Screen.About -> AboutScreen()
            }
        }
    }
}
