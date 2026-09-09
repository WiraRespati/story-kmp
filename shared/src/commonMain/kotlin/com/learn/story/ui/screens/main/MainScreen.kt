package com.learn.story.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learn.story.ui.screens.home.HomeViewModel
import com.learn.story.ui.screens.home.StoryListScreen
import com.learn.story.ui.screens.map.StoryMapScreen
import com.learn.story.ui.screens.map.StoryMapViewModel
import com.learn.story.ui.screens.profile.ProfileScreen
import com.learn.story.ui.screens.profile.ProfileViewModel
import com.learn.story.ui.screens.saved.SavedStoriesScreen
import com.learn.story.ui.screens.saved.SavedStoriesViewModel
import org.koin.compose.viewmodel.koinViewModel

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
    homeViewModel: HomeViewModel = koinViewModel(),
    mapViewModel: StoryMapViewModel = koinViewModel(),
    savedStoriesViewModel: SavedStoriesViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddStory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val savedUiState by savedStoriesViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 1: Beranda
                        CompactBottomNavItem(
                            selected = selectedTab == MainTab.HOME,
                            onClick = { selectedTab = MainTab.HOME },
                            selectedIcon = MainTab.HOME.selectedIcon,
                            unselectedIcon = MainTab.HOME.unselectedIcon,
                            title = MainTab.HOME.title
                        )

                        // Tab 2: Peta Story
                        CompactBottomNavItem(
                            selected = selectedTab == MainTab.MAP,
                            onClick = {
                                selectedTab = MainTab.MAP
                                mapViewModel.loadLocationStories()
                            },
                            selectedIcon = MainTab.MAP.selectedIcon,
                            unselectedIcon = MainTab.MAP.unselectedIcon,
                            title = MainTab.MAP.title
                        )

                        // Center Action Button: Tambah Story (Instagram/TikTok style creation button)
                        CenterActionButton(
                            onClick = onNavigateToAddStory,
                            contentDescription = "Tambah Story"
                        )

                        // Tab 4: Tersimpan & Outbox
                        CompactBottomNavItem(
                            selected = selectedTab == MainTab.SAVED,
                            onClick = { selectedTab = MainTab.SAVED },
                            selectedIcon = MainTab.SAVED.selectedIcon,
                            unselectedIcon = MainTab.SAVED.unselectedIcon,
                            title = MainTab.SAVED.title,
                            badgeCount = savedUiState.offlineDrafts.size
                        )

                        // Tab 5: Profil
                        CompactBottomNavItem(
                            selected = selectedTab == MainTab.PROFILE,
                            onClick = { selectedTab = MainTab.PROFILE },
                            selectedIcon = MainTab.PROFILE.selectedIcon,
                            unselectedIcon = MainTab.PROFILE.unselectedIcon,
                            title = MainTab.PROFILE.title
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(180)))
                },
                label = "MainTabTransition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
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
                            viewModel = savedStoriesViewModel,
                            onNavigateToDetail = onNavigateToDetail
                        )
                    }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CompactBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeCount: Int = 0
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        animationSpec = tween(200)
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .height(58.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text("$badgeCount", fontSize = 10.sp)
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun RowScope.CenterActionButton(
    onClick: () -> Unit,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            modifier = Modifier.size(42.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

