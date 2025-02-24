package com.boostcamp.mapisode.designsystem.compose.menu

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.boostcamp.mapisode.designsystem.compose.Surface
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.CLOSED_ALPHA_TARGET
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.CLOSED_SCALE_TARGET
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.EXPANDED_ALPHA_TARGET
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.EXPANDED_SCALE_TARGET
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.IN_TRANSITION_DURATION
import com.boostcamp.mapisode.designsystem.compose.menu.MapisodeDropdownMenuDefaults.OUT_TRANSITION_DURATION
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme

private object MapisodeDropdownMenuDefaults {
	const val IN_TRANSITION_DURATION = 120
	const val OUT_TRANSITION_DURATION = 75
	const val EXPANDED_SCALE_TARGET = 1f
	const val CLOSED_SCALE_TARGET = 0.8f
	const val EXPANDED_ALPHA_TARGET = 1f
	const val CLOSED_ALPHA_TARGET = 0f
}

@Composable
fun MapisodeDropdownMenu(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	offset: DpOffset = DpOffset(0.dp, 0.dp),
	content: @Composable ColumnScope.() -> Unit,
) {
	val density = LocalDensity.current
	val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }

	if (expanded) {
		Popup(
			onDismissRequest = onDismissRequest,
			popupPositionProvider = MapisodeDropdownMenuPositionProvider(
				offset,
				density,
			),
		) {
			MapisodeDropdownMenuContent(
				modifier = modifier,
				expandedState = MutableTransitionState(true),
				transformOriginState = transformOriginState,
				scrollState = ScrollState(0),
				shape = RoundedCornerShape(1.dp),
				containerColor = MapisodeTheme.colorScheme.menuBackground,
				border = BorderStroke(1.dp, MapisodeTheme.colorScheme.menuStroke),
				content = content,
			)
		}
	}
}

@Composable
internal fun MapisodeDropdownMenuContent(
	modifier: Modifier,
	expandedState: MutableTransitionState<Boolean>,
	transformOriginState: MutableState<TransformOrigin>,
	scrollState: ScrollState,
	shape: Shape,
	containerColor: Color,
	border: BorderStroke?,
	content: @Composable ColumnScope.() -> Unit,
) {
	// 메뉴 열고 닫는 애니메이션
	@Suppress("DEPRECATION")
	val transition = updateTransition(expandedState, "DropDownMenu")

	val scale by transition.animateFloat(
		transitionSpec = {
			if (false isTransitioningTo true) {
				// 확장
				tween(durationMillis = IN_TRANSITION_DURATION, easing = LinearOutSlowInEasing)
			} else {
				// 축소
				tween(durationMillis = 1, delayMillis = OUT_TRANSITION_DURATION - 1)
			}
		},
		label = "FloatAnimation",
	) { expanded ->
		if (expanded) EXPANDED_SCALE_TARGET else CLOSED_SCALE_TARGET
	}

	val alpha by transition.animateFloat(
		transitionSpec = {
			if (false isTransitioningTo true) {
				// 확장
				tween(durationMillis = 30)
			} else {
				// 축소
				tween(durationMillis = OUT_TRANSITION_DURATION)
			}
		},
		label = "FloatAnimation",
	) { expanded ->
		if (expanded) EXPANDED_ALPHA_TARGET else CLOSED_ALPHA_TARGET
	}

	val isInspecting = LocalInspectionMode.current

	Surface(
		modifier = Modifier
			.heightIn(min = 0.dp, max = 300.dp)
			.graphicsLayer {
				scaleX = if (!isInspecting) {
					scale
				} else if (expandedState.targetState) {
					EXPANDED_SCALE_TARGET
				} else {
					CLOSED_SCALE_TARGET
				}
				scaleY = if (!isInspecting) {
					scale
				} else if (expandedState.targetState) {
					EXPANDED_SCALE_TARGET
				} else {
					CLOSED_SCALE_TARGET
				}
				this.alpha = if (!isInspecting) {
					alpha
				} else if (expandedState.targetState) {
					EXPANDED_ALPHA_TARGET
				} else {
					CLOSED_ALPHA_TARGET
				}
				transformOrigin = transformOriginState.value
			},
		shape = shape,
		color = containerColor,
		border = border,
	) {
		Column(
			modifier = modifier
				.padding(vertical = 8.dp)
				.width(IntrinsicSize.Max)
				.verticalScroll(scrollState),
			content = content,
		)
	}
}

private class MapisodeDropdownMenuPositionProvider(
	private val offset: DpOffset,
	private val density: Density,
) : PopupPositionProvider {
	override fun calculatePosition(
		anchorBounds: IntRect,
		windowSize: IntSize,
		layoutDirection: LayoutDirection,
		popupContentSize: IntSize,
	): IntOffset {
		val offsetX = with(density) { offset.x.roundToPx() }
		val offsetY = with(density) { offset.y.roundToPx() }

		val x = anchorBounds.left + offsetX
		val y = anchorBounds.bottom + offsetY

		return IntOffset(
			x.coerceIn(0, windowSize.width - popupContentSize.width),
			y.coerceIn(0, windowSize.height - popupContentSize.height),
		)
	}
}
