package io.github.wechaty.user.manager

import io.github.wechaty.MockPuppet
import io.github.wechaty.Puppet
import io.github.wechaty.WechatyOptions
import io.github.wechaty.grpc.GrpcPuppet
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.utils.JsonUtils
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

const val REFLECTION_BASE_PACKAGE = "io.github.wechaty"

class PuppetManager {

    companion object {
        private val log = LoggerFactory.getLogger(PuppetManager::class.java)

        @JvmStatic
        fun resolveInstance(wechatyOptions: WechatyOptions): Future<Puppet> {
            log.info("PuppetManager resolveInstance(${JsonUtils.write(wechatyOptions)})")

//            return if ("io.github.wechaty.grpc.GrpcPuppet" == wechatyOptions.puppet) {
//                CompletableFuture.completedFuture(GrpcPuppet(wechatyOptions.puppetOptions!!))
//            } else {
//                CompletableFuture.completedFuture(MockPuppet(wechatyOptions.puppetOptions!!))
//            }
            val reflections = Reflections(REFLECTION_BASE_PACKAGE)
            val subTypes: Set<*> = reflections.getSubTypesOf(Puppet::class.java)

            for (subType in subTypes) {
                val subTypeClass = subType as Class<*>
                if (wechatyOptions.puppet == subTypeClass.canonicalName) {
                    val declaredConstructor = subTypeClass.getDeclaredConstructor(PuppetOptions::class.java)
                    return CompletableFuture.completedFuture(declaredConstructor.newInstance(wechatyOptions.puppetOptions!!) as Puppet)
                }
            }
            throw RuntimeException("instant puppet implementation error. Please check your wechatyOptions.puppet")
        }
    }


}

fun main() {
//    val serviceLoader:ServiceLoader<Puppet> = ServiceLoader.load(Puppet::class.java)
//    for (puppet in serviceLoader) {
//        println(puppet)
//    }

//    val kClass:KClass<Puppet> = Puppet::class
//    print(kClass)
    val reflections: Reflections = Reflections("io.github.wechaty")
    val subTypes: Set<*> = reflections.getSubTypesOf(Puppet::class.java)
    for (subType in subTypes) {
        val subTypeClass = subType as Class<*>
        if ("io.github.wechaty.grpc.GrpcPuppet" == subTypeClass.canonicalName) {

                val declaredConstructor = subTypeClass.getDeclaredConstructor(PuppetOptions::class.java)

                val newInstance = declaredConstructor.newInstance(PuppetOptions())

        }
    }

}

