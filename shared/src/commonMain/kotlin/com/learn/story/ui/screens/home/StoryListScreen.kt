package com.learn.story.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learn.story.data.model.Story
import com.learn.story.ui.components.EmptyStateView
import com.learn.story.ui.components.ErrorStateView
import com.learn.story.ui.components.StoryCard
import com.learn.story.ui.theme.StoryTheme

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
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                        viewModel.logout(onLoggedOut)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dicoding Story",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.userName?.let { name ->
                            Text(
                                text = "Halo, $name 👋",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (onNavigateToMap != null) {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Peta Story"
                            )
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Segarkan Data"
                        )
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Cari cerita atau nama penulis...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = !uiState.showOnlyWithLocation && !uiState.showOnlyBookmarks,
                        onClick = {
                            if (uiState.showOnlyWithLocation) onToggleLocationFilter()
                            if (uiState.showOnlyBookmarks) onToggleBookmarksFilter()
                        },
                        label = { Text("Semua (${uiState.stories.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.showOnlyWithLocation,
                        onClick = onToggleLocationFilter,
                        label = { Text("Berlokasi") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.showOnlyBookmarks,
                        onClick = onToggleBookmarksFilter,
                        label = { Text("Favorit (${uiState.bookmarkedIds.size})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }

            // Offline Outbox Banner (if there are unsynced drafts)
            if (uiState.offlineDraftsCount > 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${uiState.offlineDraftsCount} Story Offline",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tersimpan di perangkat saat offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        if (uiState.isSyncingDrafts) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            TextButton(onClick = onSyncDrafts) {
                                Text("Upload Sekarang", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Story List Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.stories.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
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
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.filteredStories, key = { it.id }) { story ->
                                StoryCard(
                                    story = story,
                                    onClick = { onNavigateToDetail(story.id) },
                                    isBookmarked = uiState.bookmarkedIds.contains(story.id),
                                    onBookmarkClick = { onToggleBookmark(story.id) }
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
