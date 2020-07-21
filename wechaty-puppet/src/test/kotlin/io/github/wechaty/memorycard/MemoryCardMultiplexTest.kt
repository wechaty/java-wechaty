package io.github.wechaty.io.github.wechaty.memorycard.test

import io.github.wechaty.memorycard.*
import io.github.wechaty.memorycard.backend.Address
import io.github.wechaty.memorycard.backend.Person
import io.github.wechaty.utils.JsonUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


class MemoryCardMultiplexTest {

    lateinit var memoryCard: MemoryCard

    @Before
    fun setUp() {
        memoryCard = MemoryCard("A")

    }

    @Test
    fun testLoadAndSave() {
        memoryCard.load()
        val cardA = memoryCard.multiplex("cardA")
        val cardB = memoryCard.multiplex("cardB")
        memoryCard.set("a", "b")
        cardA.set("a", "b")
//        cardB.set("a", "b")
        println(memoryCard.size())
        println(memoryCard.keys().size)
        println(cardA.keys().size)
        println(cardA.size())
        println(cardB.keys().size)
        memoryCard.save()
    }

    @Test
    fun testLoadAd() {
        memoryCard.load()
        val cardA = memoryCard.multiplex("cardA")
        val cardB = memoryCard.multiplex("cardB")
        println(memoryCard.size())
        println(memoryCard.keys().size)
    }

    @Test
    fun testParttern() {
        var a:String = "\rcardA\na"
        val NAMESPACE_MULTIPLEX_SEPRATOR_REGEX = Regex("\r")
        val NAMESPACE_KEY_SEPRATOR_REGEX       = Regex("\n")
        if (NAMESPACE_MULTIPLEX_SEPRATOR_REGEX.containsMatchIn(a)) {
            println(1)
        }
    }
}
