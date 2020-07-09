package io.github.wechaty.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

object JsonUtils {

    val mapper: ObjectMapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .registerModule(KotlinModule())

    inline fun <reified T> readValue(json :String): T {
        return mapper.readValue(json)
    }

    fun write(input:Any):String {
        return mapper.writeValueAsString(input)
    }

}


