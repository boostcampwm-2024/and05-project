package com.boostcamp.mapisode.mygroup.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boostcamp.mapisode.designsystem.R
import com.boostcamp.mapisode.designsystem.compose.Direction
import com.boostcamp.mapisode.designsystem.compose.MapisodeDivider
import com.boostcamp.mapisode.designsystem.compose.MapisodeIcon
import com.boostcamp.mapisode.designsystem.compose.MapisodeIconButton
import com.boostcamp.mapisode.designsystem.compose.MapisodeScaffold
import com.boostcamp.mapisode.designsystem.compose.MapisodeText
import com.boostcamp.mapisode.designsystem.compose.MapisodeTextField
import com.boostcamp.mapisode.designsystem.compose.TextAlignment
import com.boostcamp.mapisode.designsystem.compose.Thickness
import com.boostcamp.mapisode.designsystem.compose.button.MapisodeFilledButton
import com.boostcamp.mapisode.designsystem.compose.card.GroupInfoCard
import com.boostcamp.mapisode.designsystem.compose.topbar.TopAppBar
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme
import com.boostcamp.mapisode.model.GroupModel
import com.boostcamp.mapisode.mygroup.intent.GroupJoinIntent
import com.boostcamp.mapisode.mygroup.sideeffect.GroupJoinSideEffect
import com.boostcamp.mapisode.mygroup.sideeffect.rememberFlowWithLifecycle
import com.boostcamp.mapisode.mygroup.state.GroupJoinState
import com.boostcamp.mapisode.mygroup.viewmodel.GroupJoinViewModel
import com.boostcamp.mapisode.mygroup.R as S

@Composable
fun GroupJoinScreen(
	onBackClick: () -> Unit,
	viewModel: GroupJoinViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val effect = rememberFlowWithLifecycle(
		flow = viewModel.sideEffect,
		initialValue = GroupJoinSideEffect.Idle,
	).value

	LaunchedEffect(effect) {
		when (effect) {
			is GroupJoinSideEffect.NavigateToGroupScreen -> {
				onBackClick()
			}

			is GroupJoinSideEffect.ShowToast -> {
				Toast.makeText(context, effect.messageResId, Toast.LENGTH_SHORT).show()
			}

			else -> { }
		}
	}

	GroupJoinContent(
		uiState = uiState,
		onBackClick = {
			viewModel.onIntent(intent = GroupJoinIntent.OnBackClick)
		},
		onGetGroup = { joinCodeText ->
			viewModel.onIntent(intent = GroupJoinIntent.TryGetGroup(joinCodeText))
		},
		onJoinGroup = {
			viewModel.onIntent(intent = GroupJoinIntent.OnJoinClick)
		},
	)
}

@Composable
fun GroupJoinContent(
	uiState: GroupJoinState,
	onBackClick: () -> Unit,
	onGetGroup: (String) -> Unit,
	onJoinGroup: () -> Unit,
) {
	val focusManager = LocalFocusManager.current
	var joinCodeText by rememberSaveable { mutableStateOf("") }

	MapisodeScaffold(
		modifier = Modifier.pointerInput(Unit) {
			detectTapGestures(
				onPress = {
					focusManager.clearFocus()
				},
			)
		},
		isNavigationBarPaddingExist = true,
		isStatusBarPaddingExist = true,
		topBar = {
			TopAppBar(
				title = "그룹 참여",
				navigationIcon = {
					MapisodeIconButton(
						onClick = onBackClick,
					) {
						MapisodeIcon(
							id = R.drawable.ic_arrow_back_ios,
							contentDescription = "Back",
						)
					}
				},
			)
		},
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
				.padding(horizontal = 20.dp),
		) {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				item {
					Column(
						modifier = Modifier.padding(vertical = 8.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						MapisodeText(
							text = stringResource(S.string.group_join_sub_title),
							style = MapisodeTheme.typography.titleLarge,
						)
						Spacer(modifier = Modifier.padding(8.dp))
						MapisodeText(
							modifier = Modifier.fillParentMaxWidth(),
							textAlignment = TextAlignment.Center,
							text = stringResource(S.string.group_join_direction),
						)
					}
				}
				item {
					Column(
						modifier = Modifier.padding(vertical = 20.dp),
					) {
						MapisodeText(
							modifier = Modifier
								.fillParentMaxWidth()
								.padding(start = 4.dp),
							text = stringResource(S.string.group_join_textinput_label),
							style = MapisodeTheme.typography.labelLarge,
						)
						Spacer(modifier = Modifier.padding(4.dp))
						MapisodeTextField(
							modifier = Modifier.fillParentMaxWidth(),
							value = joinCodeText,
							onValueChange = { text -> joinCodeText = text },
							placeholder = stringResource(S.string.group_join_textfield_hint),
						)
					}
				}
				item {
					MapisodeFilledButton(
						modifier = Modifier
							.fillParentMaxWidth()
							.heightIn(min = 52.dp, max = 80.dp),
						onClick = {
							onGetGroup(joinCodeText)
						},
						text = stringResource(S.string.group_join_btn_direction),
						enabled = joinCodeText.length > 5,
						showRipple = true,
					)
					Spacer(modifier = Modifier.padding(10.dp))
					MapisodeDivider(direction = Direction.Horizontal, thickness = Thickness.Thin)
					Spacer(modifier = Modifier.padding(10.dp))
				}
				if (uiState.isGroupExist && uiState.group != null) {
					item {
						ConfirmJoinGroup(uiState.group.toGroupModel())
						Spacer(modifier = Modifier.padding(bottom = 70.dp))
					}
				}
				if (uiState.isGroupExist.not() || uiState.group == null) {
					item {
						MapisodeText(
							text = "존재하지 않는 그룹입니다.",
							style = MapisodeTheme.typography.bodyLarge,
						)
					}
				}
			}
			if (uiState.isGroupExist && uiState.group != null) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 30.dp)
						.align(Alignment.BottomCenter)
						.background(MapisodeTheme.colorScheme.surfaceBackground),
				) {
					Spacer(modifier = Modifier.padding(top = 4.dp))
					MapisodeDivider(direction = Direction.Horizontal, thickness = Thickness.Thin)
					Spacer(modifier = Modifier.padding(5.dp))
					MapisodeFilledButton(
						modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp, max = 80.dp),
						onClick = { onJoinGroup() },
						text = "참여하기",
						showRipple = true,
					)
				}
			}
		}
	}
}

@Composable
fun ConfirmJoinGroup(group: GroupModel) {
	Column(
		modifier = Modifier
			.padding(horizontal = 30.dp)
			.fillMaxHeight(),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		MapisodeText(
			text = stringResource(S.string.group_overview),
			modifier = Modifier.fillMaxWidth(),
			style = MapisodeTheme.typography.labelLarge,
		)
		Spacer(modifier = Modifier.padding(4.dp))

		GroupInfoCard(
			group = group,
			modifier = Modifier
				.border(
					width = 1.dp,
					color = MapisodeTheme.colorScheme.textColoredContainer,
					shape = RoundedCornerShape(12.dp),
				),
		)

		Spacer(modifier = Modifier.padding(10.dp))
		MapisodeText(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 4.dp),
			text = stringResource(S.string.group_descript),
			style = MapisodeTheme.typography.labelLarge,
		)
		Spacer(modifier = Modifier.padding(4.dp))
		Box(
			modifier = Modifier
				.border(
					width = 1.dp,
					color = MapisodeTheme.colorScheme.textColoredContainer,
					shape = RoundedCornerShape(8.dp),
				)
				.padding(10.dp)
				.wrapContentHeight()
				.heightIn(min = 100.dp),
		) {
			MapisodeText(
				modifier = Modifier.fillMaxWidth(),
				text = group.description,
			)
		}
	}
}
