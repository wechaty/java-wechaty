package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.StorageOSSOptions
import io.github.wechaty.utils.JsonUtils
import org.junit.Test
import org.junit.Assert.*;
class Person(var name: String, var age: Int, var address: Address)
class Address(var city: String, var province: String)
class StorageOSSTest {
    val storageOSSOptions = StorageOSSOptions("xxx", "xxx",
        "oss-cn-beijing.aliyuncs.com", "xxx")

    // 如果对应的name存在
    @Test
    fun testLoadExist() {
        val storageOSS = StorageOSS("objectname", storageOSSOptions)
        val load = storageOSS.load()
        println(load.map)
    }


    // 如果对应的name不存在
    @Test
    fun testLoadNotExist() {
        val storageOSS = StorageOSS("objectname", storageOSSOptions)
        val load = storageOSS.load()
        assertEquals(0, load.map.size)
    }

    @Test
    fun testSave() {
        val storageOSS = StorageOSS("objectname", storageOSSOptions)
        val load = storageOSS.load()
        load.map.put("key", "value")
        load.map.put("list", listOf(1, 2, 3))
        load.map.put("a", mapOf("a" to 1, "b" to 2))
        storageOSS.save(load)
    }

    @Test
    fun testDestory() {
        // 如果有后缀要带上完整的后缀
        val storageOSS = StorageOSS("objectname", storageOSSOptions)
        storageOSS.destory()
    }
    @Test
    fun testJson() {
        var map = mutableMapOf<String, Any>()
        var list = listOf(1, 2, 3)
        map.put("list", list)
        val write = JsonUtils.write(map)
        println(write)
        var readValue: Map<String, Any> = JsonUtils.readValue(write)
        println(readValue.get("list") is List<*>)
    }
}
