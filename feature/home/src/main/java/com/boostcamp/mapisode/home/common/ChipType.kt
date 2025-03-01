package com.boostcamp.mapisode.home.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.boostcamp.mapisode.designsystem.theme.MapisodeTheme
import com.boostcamp.mapisode.designsystem.R as Design
import com.boostcamp.mapisode.home.R as Home

enum class ChipType(
	@StringRes val textResId: Int,
	@DrawableRes val iconResId: Int,
) {
	EAT(Home.string.home_chip_eat, Design.drawable.ic_eat),
	SEE(Home.string.home_chip_see, Design.drawable.ic_see),
	OTHER(Home.string.home_chip_other, Design.drawable.ic_other),
}

@Composable
fun getChipIconTint(chipType: ChipType): Color = when (chipType) {
	ChipType.EAT -> MapisodeTheme.colorScheme.eatIconColor
	ChipType.SEE -> MapisodeTheme.colorScheme.seeIconColor
	ChipType.OTHER -> MapisodeTheme.colorScheme.otherIconColor
}

fun mapCategoryToChipType(category: String): ChipType? =
	ChipType.entries.find { it.name.equals(category, ignoreCase = true) }
