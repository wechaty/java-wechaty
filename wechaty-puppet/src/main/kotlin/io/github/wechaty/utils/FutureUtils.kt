package io.github.wechaty.utils;

import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.stream.Collectors

/**
 * @author Zhengxin
 */
object FutureUtils {

    fun <T> toCompletable(future: Future<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            try {
                return@supplyAsync future.get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
    }

    fun <T> sequence(futures: List<CompletableFuture<T>>): CompletableFuture<List<T>> {
        val allDoneFuture =
            CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<*>>())
        return allDoneFuture.thenApply { v: Void ->
            futures.stream().map { obj: CompletableFuture<T> -> obj.join() }.filter { Objects.nonNull(it) }.collect(Collectors.toList())
        }
    }

    fun sequenceVoid(futures: List<CompletableFuture<Void>>): CompletableFuture<Void> {
        val allDoneFuture =
                CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<*>>())
        return CompletableFuture.allOf(allDoneFuture)
    }
}
