package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.StorageNopOptions
import io.github.wechaty.memorycard.StorageObsOptions
import org.junit.Test


class StorageNopTest {
    @Test
    fun testLoad() {
        val storageNop = StorageNop("objectname", StorageNopOptions())
        val load = storageNop.load()
        println(load.map)
        println(storageNop)
    }

    @Test
    fun testSave() {
        val storageNop = StorageNop("myname", StorageNopOptions())
        val load = storageNop.load()
        storageNop.save(load)
    }

    @Test
    fun testDestory() {
        val storageNop = StorageNop("myname", StorageNopOptions())
        val load = storageNop.load()
        storageNop.destory()
    }
}
