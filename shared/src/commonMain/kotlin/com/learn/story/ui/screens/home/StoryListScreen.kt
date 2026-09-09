package com.learn.story.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learn.story.data.model.Story
import com.learn.story.ui.components.EmptyStateView
import com.learn.story.ui.components.ErrorStateView
import com.learn.story.ui.components.StoryCard
import com.learn.story.ui.components.StoryListSkeleton
import com.learn.story.ui.theme.StoryTheme

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryListScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddStory: () -> Unit,
    onNavigateToMap: (() -> Unit)? = null,
    onLoggedOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut && onLoggedOut != null) {
            viewModel.resetLoggedOut()
            onLoggedOut()
        }
    }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSyncMessage()
        }
    }

    if (showLogoutDialog && onLoggedOut != null) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah kamu yakin ingin keluar dari akun ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Keluar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    StoryListScreenContent(
        uiState = uiState,
        onRefresh = viewModel::loadStories,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAddStory = onNavigateToAddStory,
        onNavigateToMap = onNavigateToMap,
        onLogoutClick = if (onLoggedOut != null) { { showLogoutDialog = true } } else null,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onToggleLocationFilter = viewModel::onToggleLocationFilter,
        onToggleBookmarksFilter = viewModel::onToggleBookmarksFilter,
        onToggleBookmark = viewModel::toggleBookmark,
        onSyncDrafts = viewModel::syncOfflineDrafts,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryListScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToAddStory: () -> Unit = {},
    onNavigateToMap: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    onSearchQueryChanged: (String) -> Unit = {},
    onToggleLocationFilter: () -> Unit = {},
    onToggleBookmarksFilter: () -> Unit = {},
    onToggleBookmark: (String) -> Unit = {},
    onSyncDrafts: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    val refreshRotation by animateFloatAsState(
        targetValue = if (uiState.isLoading) 360f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "RefreshRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Story Feed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        uiState.userName?.let { name ->
                            Text(
                                text = " • $name",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Search toggle button
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded && uiState.searchQuery.isNotEmpty()) {
                            onSearchQueryChanged("")
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded || uiState.searchQuery.isNotEmpty()) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchExpanded) "Tutup Pencarian" else "Cari Cerita"
                        )
                    }

                    // Refresh button with animated rotation
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Segarkan Data",
                            modifier = Modifier.rotate(refreshRotation)
                        )
                    }

                    if (onNavigateToMap != null) {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Peta Story"
                            )
                        }
                    }

                    if (onLogoutClick != null) {
                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Keluar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Expandable Sleek Search & Filter Bar (Zero clutter when hidden)
            AnimatedVisibility(
                visible = isSearchExpanded || uiState.searchQuery.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Cari cerita atau nama penulis...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus pencarian"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.showOnlyWithLocation,
                            onClick = onToggleLocationFilter,
                            label = { Text("Hanya dengan Lokasi", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Sleek indeterminate linear indicator when refreshing in background
            AnimatedVisibility(
                visible = uiState.isLoading && uiState.stories.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Story List Content (100% focused on feed)
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.stories.isEmpty() -> {
                        // Shimmer Skeleton Loading Effect
                        StoryListSkeleton(count = 3)
                    }

                    uiState.errorMessage != null && uiState.stories.isEmpty() -> {
                        ErrorStateView(
                            message = uiState.errorMessage,
                            onRetry = onRefresh,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.filteredStories.isEmpty() -> {
                        val emptyMsg = if (uiState.searchQuery.isNotBlank()) {
                            "Tidak ada story yang cocok dengan pencarian '${uiState.searchQuery}'"
                        } else if (uiState.showOnlyBookmarks) {
                            "Belum ada story yang ditandai favorit"
                        } else if (uiState.showOnlyWithLocation) {
                            "Belum ada story yang memiliki data lokasi"
                        } else {
                            "Belum ada postingan story saat ini"
                        }
                        EmptyStateView(
                            message = emptyMsg,
                            onRefresh = onRefresh,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.filteredStories, key = { it.id }) { story ->
                                StoryCard(
                                    story = story,
                                    onClick = { onNavigateToDetail(story.id) },
                                    isBookmarked = uiState.bookmarkedIds.contains(story.id),
                                    onBookmarkClick = { onToggleBookmark(story.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


private val previewStories = listOf(
    Story(
        id = "1",
        name = "Dicoding Indonesia",
        description = "Menikmati senja di tepi pantai sambil menulis cerita seru untuk para coder. #story #coding",
        photoUrl = "https://picsum.photos/seed/story/800/500",
        createdAt = "2022-01-08T06:34:18.598Z",
        lat = -6.2088,
        lon = 106.8456
    ),
    Story(
        id = "2",
        name = "Budi",
        description = "Hari ini belajar Kotlin Multiplatform, seru banget!",
        photoUrl = "https://picsum.photos/seed/story2/800/500",
        createdAt = "2022-01-09T10:15:00.000Z"
    ),
    Story(
        id = "3",
        name = "Siti",
        description = "Coffee time di kafe favorit sambil ngoding aplikasi baru.",
        photoUrl = "https://picsum.photos/seed/story3/800/500",
        createdAt = "2022-01-10T08:00:00.000Z",
        lat = -6.9175,
        lon = 107.6191
    )
)

@Preview
@Composable
private fun StoryListScreenPreview() {
    StoryTheme {
        StoryListScreenContent(
            uiState = HomeUiState(
                stories = previewStories,
                userName = "Dicoding"
            ),
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToAddStory = {},
            onLogoutClick = {}
        )
    }
}

@Preview
@Composable
private fun StoryListScreenLoadingPreview() {
    StoryTheme {
        StoryListScreenContent(
            uiState = HomeUiState(isLoading = true),
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToAddStory = {},
            onLogoutClick = {}
        )
    }
}

@Preview
@Composable
private fun StoryListScreenEmptyPreview() {
    StoryTheme {
        StoryListScreenContent(
            uiState = HomeUiState(),
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToAddStory = {},
            onLogoutClick = {}
        )
    }
}

@Preview
@Composable
private fun StoryListScreenErrorPreview() {
    StoryTheme {
        StoryListScreenContent(
            uiState = HomeUiState(errorMessage = "Gagal memuat stories"),
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToAddStory = {},
            onLogoutClick = {}
        )
    }
}
