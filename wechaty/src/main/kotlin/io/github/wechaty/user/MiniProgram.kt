package io.github.wechaty.user

import io.github.wechaty.schemas.MiniProgramPayload

class MiniProgram(var payload: MiniProgramPayload) {

    fun appId():String?{
        return payload.appId
    }

    fun titile():String?{
        return payload.title
    }

    fun pagePath():String?{
        return payload.pagePath
    }

    fun username():String?{
        return payload.username
    }

    fun description():String?{
        return payload.description
    }

    fun thumbUrl():String? {
        return payload.thumbUrl
    }

    fun thumbKey():String?{
        return payload.thumbKey
    }


    companion object{

        fun create(): MiniProgram {
            val payload = MiniProgramPayload()
            return MiniProgram(payload);
        }

    }
}
