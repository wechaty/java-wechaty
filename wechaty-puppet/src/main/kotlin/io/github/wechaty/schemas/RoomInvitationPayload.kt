package io.github.wechaty.schemas

/**
 * @author Zhengxin
 */
class RoomInvitationPayload {
    var id: String? = null
    var inviterId: String? = null
    var topic: String? = null
    var avatar: String? = null
    var invitation: String? = null
    var memberCount: Int? = null
    var memberIdList: List<String>? = null
    var timestamp: Long? = null
    var receiverId: String? = null
}