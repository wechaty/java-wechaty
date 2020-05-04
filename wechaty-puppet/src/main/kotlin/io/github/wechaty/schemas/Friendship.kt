package io.github.wechaty.schemas

enum class FriendshipType(var code: Int) {
    Unkonwn(0), Confirm(1), Receive(2), Verify(3);

}

enum class FriendshipSceneType(var code: Int) {
    QQ(1),
    Email(2), 
    Weixin(3),
    QQtbd(12), 
    Room(14),
    Phone(15), 
    Card(17), 
    Location(18), 
    Bottle(25), 
    Shaking(29), 
    QRCode(30);

}

interface FriendshipPayload

open class FriendshipPayloadBase {
    var id: String? = null
    var contactId: String? = null
    var hello: String? = null
    var timestamp: Long? = null
}

class FriendshipPayloadConfirm : FriendshipPayloadBase(), FriendshipPayload {
    var type = FriendshipType.Confirm
}

class FriendshipPayloadReceive : FriendshipPayloadBase(), FriendshipPayload {
    var scene: FriendshipSceneType? = null
    var stranger: String? = null
    var ticket: String? = null
    var type = FriendshipType.Receive
}


class FriendshipPayloadVerify : FriendshipPayloadBase(), FriendshipPayload {
    var type = FriendshipType.Verify
}

class FriendshipSearchCondition {
    var phone: String? = null
    var weixin: String? = null
}


class FriendshipQueryFilter {
    var list: List<FriendshipSearchCondition>? = null
}