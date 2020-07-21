package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.StorageObsOptions
import org.junit.Test


class StorageObsTest {
    val storageObsOptions = StorageObsOptions("xxx", "xxx",
        "obs.cn-north-4.myhuaweicloud.com", "xxx")
    @Test
    fun testLoad() {
        val storageObs = StorageObs("objectname", storageObsOptions)
        val load = storageObs.load()
        println(load.map)
    }

    @Test
    fun testLoadNotExist() {
        // 如果name不存在会返回一个空的payload
        val storageObs = StorageObs("myname", storageObsOptions)
        val load = storageObs.load()
    }
}
