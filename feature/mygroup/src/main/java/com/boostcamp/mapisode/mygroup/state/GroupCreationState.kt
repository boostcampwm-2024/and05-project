package com.boostcamp.mapisode.mygroup.state

import androidx.compose.runtime.Immutable
import com.boostcamp.mapisode.mygroup.model.GroupCreationModel
import com.boostcamp.mapisode.ui.base.UiState

@Immutable
data class GroupCreationState(
	val isInitializing: Boolean = true,
	val isSelectingGroupImage: Boolean = false,
	val isGroupEditError: Boolean = false,
	val group: GroupCreationModel = GroupCreationModel(),
) : UiState
