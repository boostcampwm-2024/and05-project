package com.boostcamp.mapisode.episode

import androidx.core.net.toUri
import com.boostcamp.mapisode.common.util.UuidGenerator
import com.boostcamp.mapisode.episode.model.EpisodeFirestoreModel
import com.boostcamp.mapisode.episode.model.toFirestoreModelForUpdate
import com.boostcamp.mapisode.firebase.firestore.FirestoreConstants
import com.boostcamp.mapisode.firebase.firestore.StorageConstants.PATH_IMAGES
import com.boostcamp.mapisode.model.EpisodeLatLng
import com.boostcamp.mapisode.model.EpisodeModel
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class EpisodeRepositoryImpl @Inject constructor(
	private val database: FirebaseFirestore,
	private val storage: FirebaseStorage,
) : EpisodeRepository {
	private val episodeCollection = database.collection(FirestoreConstants.COLLECTION_EPISODE)
	private val groupCollection = database.collection(FirestoreConstants.COLLECTION_GROUP)

	override suspend fun getEpisodesByGroup(groupId: String): List<EpisodeModel> {
		val query = episodeCollection
			.whereEqualTo(
				FirestoreConstants.FIELD_GROUP,
				groupCollection.document(groupId),
			)
		val querySnapshot = query.get().await()
		if (querySnapshot.isEmpty) {
			return emptyList()
		}

		return try {
			querySnapshot.toDomainModelList()
		} catch (e: Exception) {
			throw e
		}
	}

	override suspend fun getEpisodesByGroupAndLocation(
		groupId: String,
		start: EpisodeLatLng,
		end: EpisodeLatLng,
		category: String?,
	): List<EpisodeModel> {
		var query = episodeCollection
			.whereEqualTo(FirestoreConstants.FIELD_GROUP, groupCollection.document(groupId))
			.whereGreaterThanOrEqualTo(
				FirestoreConstants.FIELD_LOCATION,
				GeoPoint(start.latitude, start.longitude),
			)
			.whereLessThanOrEqualTo(
				FirestoreConstants.FIELD_LOCATION,
				GeoPoint(end.latitude, end.longitude),
			)

		category?.let {
			query = query.whereEqualTo(FirestoreConstants.FIELD_CATEGORY, it)
		}

		val querySnapshot = query.get().await()

		if (querySnapshot.isEmpty) {
			return emptyList()
		}

		return try {
			querySnapshot.toDomainModelList()
		} catch (e: Exception) {
			throw e
		}
	}

	override suspend fun getEpisodesByGroupAndCategory(
		groupId: String,
		category: String,
	): List<EpisodeModel> {
		val query = episodeCollection
			.whereEqualTo(FirestoreConstants.FIELD_GROUP, groupCollection.document(groupId))
			.whereEqualTo(FirestoreConstants.FIELD_CATEGORY, category)
		val querySnapshot = query.get().await()

		if (querySnapshot.isEmpty) {
			return emptyList()
		}

		return try {
			querySnapshot.toDomainModelList()
		} catch (e: Exception) {
			throw e
		}
	}

	override suspend fun getEpisodeById(episodeId: String): EpisodeModel? =
		episodeCollection.document(episodeId)
			.get()
			.await()
			.toObject(EpisodeFirestoreModel::class.java)
			?.toDomainModel(episodeId)

	override suspend fun createEpisode(episodeModel: EpisodeModel): String {
		val newEpisodeId = UuidGenerator.generate()
		val uploadedImageUrls = uploadImagesToStorage(newEpisodeId, episodeModel.imageUrls)
		return try {
			episodeCollection
				.document(newEpisodeId)
				.set(episodeModel.toFirestoreModelForUpdate(database, uploadedImageUrls))
				.await()
			newEpisodeId
		} catch (e: Exception) {
			throw e
		}
	}

	private suspend fun uploadImagesToStorage(
		newEpisodeId: String,
		imageUris: List<String>,
	): List<String> {
		try {
			val imageStorageUrls = coroutineScope {
				imageUris.mapIndexed { index, imageUri ->
					async(Dispatchers.IO) {
						val imageRef =
							storage.reference.child("$PATH_IMAGES/$newEpisodeId/${index + 1}")
						imageRef.putFile(imageUri.toUri()).continueWithTask { task ->
							if (task.isSuccessful.not()) {
								task.exception?.let { e ->
									throw e
								}
							}
							imageRef.downloadUrl
						}.addOnCompleteListener { task ->
							if (task.isSuccessful) {
								val imageUrl = task.result
								Timber.d("image url: $imageUrl")
							} else {
								task.exception?.let { e ->
									throw e
								}
							}
						}.await()
					}
				}.awaitAll()
			}
			return imageStorageUrls.map { it.toString() }
		} catch (e: Exception) {
			throw e
		}
	}

	private fun QuerySnapshot.toDomainModelList(): List<EpisodeModel> =
		documents.map { document ->
			val model = requireNotNull(document.toObject(EpisodeFirestoreModel::class.java))
			model.toDomainModel(document.id)
		}

	override suspend fun getMostRecentEpisodeByGroup(groupId: String): EpisodeModel? {
		val query = episodeCollection
			.whereEqualTo(FirestoreConstants.FIELD_GROUP, groupCollection.document(groupId))
			.orderBy(FirestoreConstants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
			.limit(1)
		val querySnapshot = query.get().await()

		if (querySnapshot.isEmpty) {
			return null
		}

		return try {
			querySnapshot.documents.first().toObject(EpisodeFirestoreModel::class.java)
				?.toDomainModel(querySnapshot.documents.first().id)
		} catch (e: Exception) {
			throw e
		}
	}

	override suspend fun updateEpisode(episodeModel: EpisodeModel) {
		try {
			val updatedUrls = mutableListOf<String>()
			episodeModel.imageUrls.forEachIndexed { index, _ ->
				val imageRef =
					storage.reference.child("$PATH_IMAGES/${episodeModel.id}/${index + 1}")
				val downloadUrl = imageRef.downloadUrl.await()
				updatedUrls.add(downloadUrl.toString())
			}
			episodeModel.imageUrlsUsedForOnlyUpdate.forEachIndexed { index, imageUri ->
				val imageRef =
					storage.reference.child(
						"$PATH_IMAGES/${episodeModel.id}/${index + episodeModel.imageUrls.size + 1}",
					)
				val uploadTask = imageRef.putFile(imageUri.toUri()).await()
				val downloadUrl = uploadTask.task.result.storage.downloadUrl.await()
				updatedUrls.add(downloadUrl.toString())
			}

			val createdBy = database.collection(
				FirestoreConstants.COLLECTION_USER,
			).document(episodeModel.createdBy)
			val group = database.collection(
				FirestoreConstants.COLLECTION_GROUP,
			).document(episodeModel.group)
			database.document(
				"${FirestoreConstants.COLLECTION_EPISODE}/${episodeModel.id}",
			)
				.set(episodeModel.toFirestoreModelForUpdate(createdBy, group, updatedUrls))
				.await()
		} catch (e: FirebaseException) {
			// Firebase 관련 예외 처리
			throw e
		} catch (e: Exception) {
			throw e
		}
	}
}
