package io.github.wechaty.memorycard

import org.junit.Test


class StorageOptionsTest {

    @Test
    fun testBackendDict() {
        BACKEND_DICT.forEach { t, u ->
            println("key:" + t + " value:" + u)
        }
    }
}
