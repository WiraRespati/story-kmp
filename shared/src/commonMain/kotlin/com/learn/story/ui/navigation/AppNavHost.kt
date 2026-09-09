package com.learn.story.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learn.story.data.repository.AuthRepository
import com.learn.story.ui.screens.add.AddStoryScreen
import com.learn.story.ui.screens.add.AddStoryViewModel
import com.learn.story.ui.screens.add.SelectLocationScreen
import com.learn.story.ui.screens.auth.LoginScreen
import com.learn.story.ui.screens.auth.LoginViewModel
import com.learn.story.ui.screens.auth.RegisterScreen
import com.learn.story.ui.screens.auth.RegisterViewModel
import com.learn.story.ui.screens.detail.DetailStoryScreen
import com.learn.story.ui.screens.detail.DetailViewModel
import com.learn.story.ui.screens.home.HomeViewModel
import com.learn.story.ui.screens.home.StoryListScreen
import com.learn.story.ui.screens.map.StoryMapScreen
import com.learn.story.ui.screens.map.StoryMapViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.savedstate.read

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DETAIL = "detail/{storyId}"
    const val ADD_STORY = "add_story"
    const val SELECT_LOCATION = "select_location"
    const val MAP = "map"

    fun detail(storyId: String) = "detail/$storyId"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = koinInject()
) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isUserLoggedIn()) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.LOGIN) {
            val viewModel = koinViewModel<LoginViewModel>()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            val viewModel = koinViewModel<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val homeViewModel = koinViewModel<HomeViewModel>()
            val mapViewModel = koinViewModel<StoryMapViewModel>()
            com.learn.story.ui.screens.main.MainScreen(
                homeViewModel = homeViewModel,
                mapViewModel = mapViewModel,
                onNavigateToDetail = { storyId ->
                    navController.navigate(Routes.detail(storyId))
                },
                onNavigateToAddStory = {
                    navController.navigate(Routes.ADD_STORY)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAP) {
            val viewModel = koinViewModel<StoryMapViewModel>()
            StoryMapScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                onNavigateToDetail = { storyId ->
                    navController.navigate(Routes.detail(storyId))
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.read { getString("storyId") }.orEmpty()
            val viewModel = koinViewModel<DetailViewModel>()
            DetailStoryScreen(
                storyId = storyId,
                viewModel = viewModel,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.ADD_STORY) {
            val viewModel = koinViewModel<AddStoryViewModel>()
            AddStoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                onNavigateToSelectLocation = {
                    navController.navigate(Routes.SELECT_LOCATION)
                },
                onUploadSuccess = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.SELECT_LOCATION) {
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(Routes.ADD_STORY)
            }
            val viewModel = koinViewModel<AddStoryViewModel>(viewModelStoreOwner = parentEntry)
            SelectLocationScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
