package io.github.wechaty.schemas


enum class ContactGender(var code: Int) {
    Unknown(0), Male(1), Female(2);

    companion object {
        fun getByCode(code: Int): ContactGender {
            val values = values()
            for (value in values) {
                if (value.code == code) {
                    return value
                }
            }
            return Unknown
        }
    }
}

enum class ContactType(var code: Int) {
    Unknown(0), Personal(1), Official(2);

    companion object {
        fun getByCode(code: Int): ContactType {
            val values = values()
            for (value in values) {
                if (value.code == code) {
                    return value
                }
            }
            return Unknown
        }
    }


}

class ContactQueryFilter {
    var alias: String? = null
    var id: String? = null
    var name: String? = null
    var weixin: String? = null

    var aliasReg:Regex? = null
    var nameReg:Regex? = null
}

class ContactPayload(val id:String) {
    var gender: ContactGender? = null
    var type: ContactType? = null
    var name: String? = null
    var avatar: String? = null
    var address: String? = null
    var alias: String? = null
    var city: String? = null
    var friend: Boolean? = null
    var province: String? = null
    var signature: String? = null
    var star: Boolean? = null
    var weixin: String? = null
    override fun toString(): String {
        return "ContactPayload(id=$id, gender=$gender, type=$type, name=$name, avatar=$avatar, address=$address, alias=$alias, city=$city, friend=$friend, province=$province, signature=$signature, star=$star, weixin=$weixin)"
    }
}

typealias ContactPayloadFilterFunction = (payload:ContactPayload) -> Boolean
typealias ContactPayloadFilterFactory  = (query: ContactQueryFilter) -> ContactPayloadFilterFunction

