package com.boostcamp.mapisode.episode.intent

import com.boostcamp.mapisode.ui.base.UiIntent
import com.naver.maps.geometry.LatLng
import android.net.Uri
import kotlinx.collections.immutable.PersistentList
import java.util.Date

sealed class NewEpisodeIntent : UiIntent {
	data class SetEpisodePics(val pics: PersistentList<Uri>) : NewEpisodeIntent()
	data class SetEpisodeLocation(val latLng: LatLng) : NewEpisodeIntent()
	data class SetEpisodeGroup(val group: String) : NewEpisodeIntent()
	data class SetEpisodeCategory(val category: String) : NewEpisodeIntent()
	data class SetEpisodeTags(val tags: String) : NewEpisodeIntent()
	data class SetEpisodeDate(val date: Date) : NewEpisodeIntent()
	data class SetEpisodeInfo(val episodeInfo: NewEpisodeInfo) : NewEpisodeIntent()
	data class SetEpisodeContent(val episodeContent: NewEpisodeContent) : NewEpisodeIntent()
	data object CreateNewEpisode : NewEpisodeIntent()
}
