package io.github.wechaty.schemas

class RoomMemberQueryFilter() {
    var name: String? = null
    var roomAlias: String? = null
    var contactAlias: String? = null
}


class RoomQueryFilter {
    var id: String? = null
    var topic: String? = null

    var topicRegex:Regex? =null
}


class RoomPayload (val id:String){
    var topic: String? = null
    var avatar: String? = null
    var memberIdList: List<String> = listOf()
    var ownerId: String? = null
    var adminIdList: List<String>? = null
}

class RoomMemberPayload {
    var id: String? = null
    var roomAlias: String? = null
    var inviterId: String? = null
    var avatar: String? = null
    var name: String? = null
}
