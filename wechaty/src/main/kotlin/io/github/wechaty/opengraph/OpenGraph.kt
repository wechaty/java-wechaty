package io.github.wechaty.opengraph

import org.opengraph.OpenGraph
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

fun openGraph(url:String):Future<OpenGraph>{
    return CompletableFuture.supplyAsync {
        return@supplyAsync OpenGraph(url,true)
    }
}
