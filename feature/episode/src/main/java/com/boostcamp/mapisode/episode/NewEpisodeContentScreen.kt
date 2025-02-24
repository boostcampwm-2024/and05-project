package com.boostcamp.mapisode.episode

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.boostcamp.mapisode.designsystem.compose.MapisodeCircularLoadingIndicator
import com.boostcamp.mapisode.designsystem.compose.MapisodeScaffold
import com.boostcamp.mapisode.designsystem.compose.MapisodeText
import com.boostcamp.mapisode.designsystem.compose.Surface
import com.boostcamp.mapisode.designsystem.compose.button.MapisodeThrottleFilledButton
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme
import com.boostcamp.mapisode.episode.common.NewEpisodeConstant.buttonModifier
import com.boostcamp.mapisode.episode.common.NewEpisodeConstant.textFieldModifier
import com.boostcamp.mapisode.episode.common.NewEpisodeConstant.textFieldVerticalArrangement
import com.boostcamp.mapisode.episode.intent.NewEpisodeContent
import com.boostcamp.mapisode.episode.intent.NewEpisodeIntent
import com.boostcamp.mapisode.episode.intent.NewEpisodeSideEffect
import com.boostcamp.mapisode.episode.intent.NewEpisodeViewModel
import timber.log.Timber

@Composable
internal fun NewEpisodeContentScreen(
	viewModel: NewEpisodeViewModel = hiltViewModel(),
	onPopBack: () -> Unit,
	onPopBackToMain: () -> Unit,
) {
	val context = LocalContext.current
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	var titleValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
		mutableStateOf(TextFieldValue(uiState.episodeContent.title))
	}
	var descriptionValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
		mutableStateOf(TextFieldValue(uiState.episodeContent.description))
	}
	var showClearDialog by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		viewModel.sideEffect.collect { sideEffect ->
			when (sideEffect) {
				is NewEpisodeSideEffect.ShowToast -> {
					Toast.makeText(
						context,
						sideEffect.messageResId,
						Toast.LENGTH_SHORT,
					).show()
				}

				is NewEpisodeSideEffect.NavigateBackToHome -> {
					titleValue = TextFieldValue("")
					descriptionValue = TextFieldValue("")
					onPopBackToMain()
				}
			}
		}
	}

	if (showClearDialog) {
		ClearDialog(
			onResultRequest = { result ->
				if (result) {
					viewModel.onIntent(NewEpisodeIntent.ClearEpisode)
					onPopBackToMain()
				}
			},
			onDismissRequest = {
				showClearDialog = false
			},
		)
	}

	MapisodeScaffold(
		topBar = {
			NewEpisodeTopBar(
				onClickBack = {
					onPopBack()
				},
				onClickClear = {
					showClearDialog = true
				},
			)
		},
		isStatusBarPaddingExist = true,
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
				.padding(20.dp),
			verticalArrangement = Arrangement.SpaceBetween,
		) {
			Column {
				EpisodeTextFieldGroup(
					labelRes = R.string.new_episode_content_title,
					placeholderRes = R.string.new_episode_content_placeholder_title,
					value = titleValue.text,
					onValueChange = { titleValue = TextFieldValue(it, TextRange(it.length)) },
				)
				EpisodeTextFieldGroup(
					modifier = Modifier.fillMaxHeight(0.3f),
					labelRes = R.string.new_episode_content_description,
					placeholderRes = R.string.new_episode_content_placeholder_description,
					value = descriptionValue.text,
					onValueChange = { descriptionValue = TextFieldValue(it, TextRange(it.length)) },
				)
				Column(textFieldModifier, verticalArrangement = textFieldVerticalArrangement) {
					MapisodeText(
						text = stringResource(R.string.new_episode_content_image),
						style = MapisodeTheme.typography.labelLarge,
					)
					LazyRow(
						contentPadding = PaddingValues(12.dp),
						horizontalArrangement = Arrangement.spacedBy(10.dp),
					) {
						items(uiState.episodeContent.images) { imageUri ->
							Surface(Modifier.size(150.dp)) {
								AsyncImage(
									model = imageUri,
									contentDescription = null,
								)
							}
						}
					}
				}
			}
			MapisodeThrottleFilledButton(
				modifier = buttonModifier,
				onClick = {
					if (uiState.isCreatingEpisode.not()) {
						viewModel.onIntent(NewEpisodeIntent.SetIsCreatingEpisode(true))
						viewModel.onIntent(
							NewEpisodeIntent.SetEpisodeContent(
								NewEpisodeContent(
									titleValue.text,
									descriptionValue.text,
									uiState.episodeContent.images,
								),
							),
						)
						try {
							viewModel.onIntent(NewEpisodeIntent.CreateNewEpisode)
						} catch (e: Exception) {
							Timber.e(e)
						}
					}
				},
				text = stringResource(R.string.new_episode_create_episode),
				disabledText = stringResource(R.string.new_episode_creating_episode),
				enabled = uiState.isCreatingEpisode.not(),
			)
		}
	}

	if (uiState.isCreatingEpisode) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.clickable(
					enabled = false,
					onClick = {},
				)
				.background(
					color = Color.Black.copy(alpha = 0.3f),
				),
			contentAlignment = Alignment.Center,
		) {
			MapisodeCircularLoadingIndicator()
		}
	}
}

@Preview
@Composable
internal fun NewEpisodeContentScreenPreview() {
	NewEpisodeContentScreen(
		onPopBack = {},
		onPopBackToMain = {},
	)
}
