package com.boostcamp.mapisode.designsystem.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boostcamp.mapisode.designsystem.compose.toast.ToastHost
import com.boostcamp.mapisode.designsystem.compose.toast.ToastHostState
import com.boostcamp.mapisode.designsystem.compose.toast.rememberToastHostState
import com.boostcamp.mapisode.designsystem.theme.LocalMapisodeContentColor
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme

private enum class ScaffoldLayoutContent { TopBar, MainContent, Snackbar, BottomBar }

@Composable
fun MapisodeScaffold(
	modifier: Modifier = Modifier,
	toastHostState: ToastHostState = rememberToastHostState(),
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	toastHost: @Composable (ToastHostState) -> Unit = { ToastHost(it) },
	backgroundColor: Color = MapisodeTheme.colorScheme.background,
	contentColor: Color = LocalMapisodeContentColor.current,
	content: @Composable (PaddingValues) -> Unit,
) {
	Surface(modifier = modifier, color = backgroundColor, contentColor = contentColor) {
		ScaffoldLayout(
			topBar = topBar,
			bottomBar = bottomBar,
			toast = { toastHost(toastHostState) },
			content = content,
		)
	}
}

enum class ToastPosition(val offsetFromBottom: Dp) {
	Default(72.dp),
}

@Composable
private fun ScaffoldLayout(
	topBar: @Composable () -> Unit,
	bottomBar: @Composable () -> Unit,
	toast: @Composable () -> Unit,
	content: @Composable (PaddingValues) -> Unit,
) {
	val pxValue = LocalDensity.current.run { ToastPosition.Default.offsetFromBottom.toPx() }.toInt()

	SubcomposeLayout { constraints ->
		val layoutWidth = constraints.maxWidth
		val layoutHeight = constraints.maxHeight

		val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

		layout(layoutWidth, layoutHeight) {

			val topBarPlaceables = subcompose(ScaffoldLayoutContent.TopBar, topBar).map {
				it.measure(looseConstraints)
			}

			val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

			val toastPlaceables = subcompose(ScaffoldLayoutContent.Snackbar, toast).map {
				it.measure(looseConstraints)
			}

			val toastHeight = toastPlaceables.maxByOrNull { it.height }?.height ?: 0

			val bottomBarPlaceables = subcompose(ScaffoldLayoutContent.BottomBar, bottomBar).map {
				it.measure(looseConstraints)
			}

			val bottomBarHeight = bottomBarPlaceables.maxByOrNull { it.height }?.height ?: 0

			val snackbarOffsetFromBottom = if (toastHeight != 0) {
				toastHeight + pxValue
			} else {
				0
			}

			val bodyContentHeight = layoutHeight - topBarHeight

			val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
				val innerPadding = PaddingValues(bottom = bottomBarHeight.toDp())
				content(innerPadding)
			}.map { it.measure(looseConstraints.copy(maxHeight = bodyContentHeight)) }

			bodyContentPlaceables.forEach {
				it.place(0, topBarHeight)
			}
			topBarPlaceables.forEach {
				it.place(0, 0)
			}
			toastPlaceables.forEach {
				it.place(0, layoutHeight - snackbarOffsetFromBottom)
			}
			bottomBarPlaceables.forEach {
				it.place(0, layoutHeight - bottomBarHeight)
			}
		}
	}
}
