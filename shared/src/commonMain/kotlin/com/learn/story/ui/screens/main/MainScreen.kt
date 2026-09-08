package com.learn.story.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learn.story.ui.screens.home.HomeViewModel
import com.learn.story.ui.screens.home.StoryListScreen
import com.learn.story.ui.screens.map.StoryMapScreen
import com.learn.story.ui.screens.map.StoryMapViewModel
import com.learn.story.ui.screens.profile.ProfileScreen
import com.learn.story.ui.screens.saved.SavedStoriesScreen

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    MAP("Peta", Icons.Filled.Map, Icons.Outlined.Map),
    SAVED("Tersimpan", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    mapViewModel: StoryMapViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddStory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Tab 1: Beranda
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.HOME) MainTab.HOME.selectedIcon else MainTab.HOME.unselectedIcon,
                            contentDescription = MainTab.HOME.title
                        )
                    },
                    label = {
                        Text(
                            text = MainTab.HOME.title,
                            fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 2: Peta Story
                NavigationBarItem(
                    selected = selectedTab == MainTab.MAP,
                    onClick = {
                        selectedTab = MainTab.MAP
                        mapViewModel.loadLocationStories()
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.MAP) MainTab.MAP.selectedIcon else MainTab.MAP.unselectedIcon,
                            contentDescription = MainTab.MAP.title
                        )
                    },
                    label = {
                        Text(
                            text = MainTab.MAP.title,
                            fontWeight = if (selectedTab == MainTab.MAP) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 3: Tambah Story (Center Action)
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAddStory,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Tambah Story",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = {
                        Text(
                            text = "Tambah",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 4: Tersimpan & Outbox
                NavigationBarItem(
                    selected = selectedTab == MainTab.SAVED,
                    onClick = { selectedTab = MainTab.SAVED },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (homeUiState.offlineDraftsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text("${homeUiState.offlineDraftsCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == MainTab.SAVED) MainTab.SAVED.selectedIcon else MainTab.SAVED.unselectedIcon,
                                contentDescription = MainTab.SAVED.title
                            )
                        }
                    },
                    label = {
                        Text(
                            text = MainTab.SAVED.title,
                            fontWeight = if (selectedTab == MainTab.SAVED) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 5: Profil
                NavigationBarItem(
                    selected = selectedTab == MainTab.PROFILE,
                    onClick = { selectedTab = MainTab.PROFILE },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == MainTab.PROFILE) MainTab.PROFILE.selectedIcon else MainTab.PROFILE.unselectedIcon,
                            contentDescription = MainTab.PROFILE.title
                        )
                    },
                    label = {
                        Text(
                            text = MainTab.PROFILE.title,
                            fontWeight = if (selectedTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                MainTab.HOME -> {
                    StoryListScreen(
                        viewModel = homeViewModel,
                        onNavigateToDetail = onNavigateToDetail,
                        onNavigateToAddStory = onNavigateToAddStory,
                        onNavigateToMap = null,
                        onLoggedOut = null
                    )
                }

                MainTab.MAP -> {
                    StoryMapScreen(
                        viewModel = mapViewModel,
                        onNavigateBack = null,
                        onNavigateToDetail = onNavigateToDetail
                    )
                }

                MainTab.SAVED -> {
                    SavedStoriesScreen(
                        uiState = homeUiState,
                        onNavigateToDetail = onNavigateToDetail,
                        onToggleBookmark = homeViewModel::toggleBookmark,
                        onSyncDrafts = homeViewModel::syncOfflineDrafts,
                        onDeleteDraft = homeViewModel::deleteOfflineDraft,
                        onRefresh = homeViewModel::loadStories
                    )
                }

                MainTab.PROFILE -> {
                    ProfileScreen(
                        uiState = homeUiState,
                        onRefresh = homeViewModel::loadStories,
                        onSyncDrafts = homeViewModel::syncOfflineDrafts,
                        onLogout = { homeViewModel.logout(onLogout) }
                    )
                }
            }
        }
    }
}
