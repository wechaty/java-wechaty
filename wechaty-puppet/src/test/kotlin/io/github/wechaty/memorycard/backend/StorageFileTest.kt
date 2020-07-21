package io.github.wechaty.io.github.wechaty.memorycard.test

import io.github.wechaty.memorycard.MemoryCardPayload
import io.github.wechaty.memorycard.StorageFileOptions
import io.github.wechaty.memorycard.backend.StorageFile
import org.junit.Before
import org.junit.Test


class StorageFileTset {

    lateinit var storageFile: StorageFile

    @Before
    fun setUp() {
        this.storageFile = StorageFile("test", StorageFileOptions())
    }
    @Test
    fun testCreate() {
        println(this.storageFile.name)
    }

    @Test
    fun testLoad() {
        val load = this.storageFile.load()
        for (mutableEntry in load.map) {
            println("key:" + mutableEntry.key + " value:" + mutableEntry.value)
        }
    }

    @Test
    fun testSave() {
        val memoryCardPayload = MemoryCardPayload()

        memoryCardPayload.map.put("a", 123)
        memoryCardPayload.map.put("b", "年后")
        memoryCardPayload.map.put("c", "今年过节不收礼")
        storageFile.save(memoryCardPayload)
    }
    @Test
    fun testDestory() {
        storageFile.destory()

    }
}
