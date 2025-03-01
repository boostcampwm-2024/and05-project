package com.boostcamp.mapisode.home.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.boostcamp.mapisode.home.HomeRoute
import com.boostcamp.mapisode.home.detail.EpisodeDetailRoute
import com.boostcamp.mapisode.home.edit.EpisodeEditRoute
import com.boostcamp.mapisode.home.list.EpisodeListRoute
import com.boostcamp.mapisode.home.story.StoryViewerRoute
import com.boostcamp.mapisode.model.EpisodeLatLng
import com.boostcamp.mapisode.navigation.HomeRoute
import com.boostcamp.mapisode.navigation.MainRoute

fun NavController.navigateHome(
	navOptions: NavOptions? = null,
) {
	navigate(MainRoute.Home, navOptions)
}

fun NavController.navigateEpisodeDetail(
	episodeId: String,
	navOptions: NavOptions? = null,
) {
	navigate(HomeRoute.Detail(episodeId), navOptions)
}

fun NavController.navigateEpisodeList(
	groupId: String,
	navOptions: NavOptions? = null,
) {
	navigate(HomeRoute.List(groupId), navOptions)
}

fun NavController.navigateEpisodeEdit(
	episodeId: String,
	navOptions: NavOptions? = null,
) {
	navigate(HomeRoute.Edit(episodeId), navOptions)
}

fun NavController.navigateStoryViewer(
	navOptions: NavOptions? = null,
) {
	navigate(HomeRoute.Story, navOptions)
}

fun NavGraphBuilder.addHomeNavGraph(
	navController: NavController,
	onTextMarkerClick: (EpisodeLatLng) -> Unit,
	onEpisodeEditClick: (String) -> Unit,
	onEpisodeClick: (String) -> Unit,
	onListFabClick: (String) -> Unit,
	onStoryClick: () -> Unit,
	onBackClick: () -> Unit,
) {
	composable<MainRoute.Home> {
		HomeRoute(
			onTextMarkerClick = onTextMarkerClick,
			onEpisodeClick = onEpisodeClick,
			onListFabClick = onListFabClick,
		)
	}

	composable<HomeRoute.Detail> { backStackEntry ->
		val episodeId = backStackEntry.toRoute<HomeRoute.Detail>().episodeId
		EpisodeDetailRoute(
			episodeId = episodeId,
			onEpisodeEditClick = onEpisodeEditClick,
			onOpenStoryViewer = onStoryClick,
			onBackClick = onBackClick,
		)
	}

	composable<HomeRoute.List> { backStackEntry ->
		val groupId = backStackEntry.toRoute<HomeRoute.List>().groupId
		EpisodeListRoute(groupId = groupId, onBackClick = onBackClick)
	}

	composable<HomeRoute.Edit> { backStackEntry ->
		val episodeId = backStackEntry.toRoute<HomeRoute.Edit>().episodeId
		EpisodeEditRoute(episodeId = episodeId, onBackClick = onBackClick)
	}

	composable<HomeRoute.Story> { backStackEntry ->
		val parentEntry = remember(backStackEntry) {
			navController.getBackStackEntry<HomeRoute.Detail>()
		}

		StoryViewerRoute(
			viewModel = hiltViewModel(parentEntry),
			onBackClick = onBackClick,
		)
	}
}
