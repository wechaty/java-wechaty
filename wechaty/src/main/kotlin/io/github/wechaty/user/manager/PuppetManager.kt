package io.github.wechaty.user.manager

import io.github.wechaty.Puppet
import io.github.wechaty.WechatyOptions
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.utils.JsonUtils
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
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

            val reflections = Reflections(ClasspathHelper.forPackage(REFLECTION_BASE_PACKAGE, Thread.currentThread().contextClassLoader))
            val subTypes: Set<*> = reflections.getSubTypesOf(Puppet::class.java)

            if (subTypes.size > 1) {
                throw RuntimeException("expect one puppet,but found ${subTypes.size}")
            }
            val clazz = subTypes.first() as Class<*>
            val declaredConstructor = clazz.getDeclaredConstructor(PuppetOptions::class.java)
            return CompletableFuture.completedFuture(declaredConstructor.newInstance(wechatyOptions.puppetOptions!!) as Puppet)
        }
    }


}


