package com.boostcamp.mapisode.mygroup

import com.boostcamp.mapisode.model.EpisodeModel
import com.boostcamp.mapisode.model.GroupMemberModel
import com.boostcamp.mapisode.model.GroupModel

interface GroupRepository {
	suspend fun getGroupByGroupId(groupId: String): GroupModel
	suspend fun getGroupsByUserId(userId: String): List<GroupModel>
	suspend fun getGroupByInviteCodes(inviteCodes: String): GroupModel
	suspend fun joinGroup(userId: String, groupId: String)
	suspend fun issueInvitationCode(groupId: String): String
	suspend fun leaveGroup(userId: String, groupId: String)
	suspend fun createGroup(groupModel: GroupModel)
	suspend fun updateGroup(groupModel: GroupModel)
	suspend fun deleteGroup(groupId: String)

	suspend fun getUserInfoByUserId(userId: String): GroupMemberModel
	suspend fun getEpisodesByGroupIdAndUserId(groupId: String, userId: String): List<EpisodeModel>
}
