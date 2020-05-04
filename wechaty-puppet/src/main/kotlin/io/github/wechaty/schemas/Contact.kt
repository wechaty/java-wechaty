package io.github.wechaty.schemas


enum class ContractGender(var code: Int) {
    Unknow(0), Male(1), Female(2);

    companion object {
        fun getByCode(code: Int): ContractGender {
            val values = values()
            for (value in values) {
                if (value.code == code) {
                    return value
                }
            }
            return Unknow
        }
    }
}

enum class ContractType(var code: Int) {
    Unknow(0), Personal(1), Official(2);

    companion object {
        fun getByCode(code: Int): ContractType {
            val values = values()
            for (value in values) {
                if (value.code == code) {
                    return value
                }
            }
            return Unknow
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
}
