package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnimeBottomNav
import com.example.ui.components.AnimeStudioTab
import com.example.ui.components.AnimeTopBar
import com.example.ui.screens.CharactersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AnimeStudioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AnimeStudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AnimeStudioApp(viewModel: AnimeStudioViewModel) {
    var currentTab by remember { mutableStateOf(AnimeStudioTab.STUDIO) }

    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val scenes by viewModel.activeScenes.collectAsStateWithLifecycle()

    // Handle back button to return to STUDIO tab if on sub-screen
    if (currentTab != AnimeStudioTab.STUDIO) {
        BackHandler {
            currentTab = AnimeStudioTab.STUDIO
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            AnimeTopBar(
                currentProjectTitle = activeProject?.title ?: "VIKAS AI ANIME",
                totalScenes = scenes.size,
                onQuickExportClick = {
                    currentTab = AnimeStudioTab.CINEMA
                }
            )
        },
        bottomBar = {
            AnimeBottomNav(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
        },
        containerColor = AnimeDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AnimeStudioTab.STUDIO -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateTab = { tab -> currentTab = tab }
                    )
                }
                AnimeStudioTab.CHARACTERS -> {
                    CharactersScreen(
                        viewModel = viewModel
                    )
                }
                AnimeStudioTab.TIMELINE -> {
                    TimelineScreen(
                        viewModel = viewModel
                    )
                }
                AnimeStudioTab.CINEMA -> {
                    PlayerScreen(
                        viewModel = viewModel
                    )
                }
                AnimeStudioTab.PROJECTS -> {
                    ProjectsScreen(
                        viewModel = viewModel,
                        onNavigateTab = { tab -> currentTab = tab }
                    )
                }
            }
        }
    }
}
