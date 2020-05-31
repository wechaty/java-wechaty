package wechaty.utils

import org.mockito.Mockito

/**
 * A helper for Mockito
 *
 * @author renxiaoya
 * @date 2020-05-29
 **/
object MockitoHelper {
    fun <T> anyObject(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> uninitialized(): T = null as T
}
