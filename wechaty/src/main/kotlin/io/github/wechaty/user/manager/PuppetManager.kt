package io.github.wechaty.user.manager

import io.github.wechaty.MockPuppet
import io.github.wechaty.Puppet
import io.github.wechaty.WechatyOptions
import io.github.wechaty.grpc.GrpcPuppet
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class PuppetManager {

    companion object {
        private val log = LoggerFactory.getLogger(MockPuppet::class.java)

        @JvmStatic
        fun resolveInstance(wechatyOptions: WechatyOptions): Future<Puppet> {
            log.info("PuppetManager resolveInstance(${JsonUtils.write(wechatyOptions)})")

            return if ("wechaty-puppet-hostie" == wechatyOptions.puppet) {
                CompletableFuture.completedFuture(GrpcPuppet(wechatyOptions.puppetOptions!!))
            } else {
                CompletableFuture.completedFuture(MockPuppet(wechatyOptions.puppetOptions!!))
            }
        }
    }


}
