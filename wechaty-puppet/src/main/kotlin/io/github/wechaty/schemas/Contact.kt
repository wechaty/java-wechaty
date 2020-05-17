package io.github.wechaty.schemas


enum class ContractGender(var code: Int) {
    Unknown(0), Male(1), Female(2);

    companion object {
        fun getByCode(code: Int): ContractGender {
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

enum class ContractType(var code: Int) {
    Unknown(0), Personal(1), Official(2);

    companion object {
        fun getByCode(code: Int): ContractType {
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
}

class ContactPayload {
    var id: String? = null
    var gender: ContractGender? = null
    var type: ContractType? = null
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
