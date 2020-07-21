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


class MemoryCardTest {

    lateinit var memoryCard: MemoryCard

    @Before
    fun setUp() {
        memoryCard = MemoryCard("test")
    }

    @Test
    fun testCreate() {
        this.memoryCard.load()
        this.memoryCard.values().forEach {
            assertEquals("b", it)
        }
    }

    @Test
    fun testLoadAsync() {
        runBlocking {
            var job = GlobalScope.launch {
                memoryCard.loadAsync()
            }
            job.join()
        }

        println(memoryCard.keys().size)
    }

    @Test
    fun testSet() {
        this.memoryCard.load()
        this.memoryCard.set("list", listOf(1, 2, 3))
        this.memoryCard.save()
    }

    @Test
    fun testDefultSave() {
        var son = MemoryCard()
        son.load()
        son.set("a", "你好")
        var map = mapOf("key" to "value")
        son.set("this is map", map)
        son.keys().forEach {
            println(it)
        }
        son.save()
    }

    @Test
    fun testDelete() {
        memoryCard.load()
        memoryCard.delete("a")
        memoryCard.save()
    }
    @Test
    fun testDestory() {
        var de = MemoryCard()
        de.load()
        de.destory()
    }

    @Test
    fun testMultiplex() {
        memoryCard.load()
        // 这里的name是前缀的名字
        // \rparent\nson
        val multiplex = Multiplex(this.memoryCard, "parent")
        val memoryCardOptions = MemoryCardOptions("son", multiplex = multiplex)
        println("options:" + memoryCardOptions)
        // 用debug模式会有问题
        // 如果一个memory是multiple的,那么它自身就不会保存,只会保存一个父类的
        var son = MemoryCard(options = memoryCardOptions)
        son.load()

        son.set("son", "b")
        son.set("nihao", "asdasd")
        println(son.get("son"))
        son.delete("nihao")
        println("has key{nihao}:" + son.has("nihao"))
        println("has key{son}:" + son.has("son"))
        println("has key{list}:" + son.has("list"))
        son.save()
    }

    @Test
    fun newLoad() {
        var card = MemoryCard()
        card.load()
        val get = card.get("person") as Map<*, *>
        println(get["name"])

        var address = Address("福州", "付件")
        var person = Person("sda", 13, address)
        var text = JsonUtils.write(person)
        println(text)
        var city = JsonUtils.readValue<Map<String, Any>>(text)["address"] as Map<*, *>
        println(city["province"])
    }

    @Test
    fun testMap() {
        var card = MemoryCard()
        card.load()
        println(card.keys().size)
        for (value in card.values()) {
            println(value)
        }
        card.keys().forEach {
            println(it)
        }

        card.entries().forEach {
            println(it.key + ":" + it.value)
        }
        for (entry in card.get("person") as Map<String, Any>) {
            println(entry.key + ":" + entry.value)
        }
    }

    @Test
    fun testMultiplexSave() {
        // 设置父memorycard
        var parentCard = MemoryCard("aaaa")
        parentCard.load()
        parentCard.set("a", "b")
        // \r后面的为parent
        val multiplex = Multiplex(parentCard, "parent")
        // 子card的名字为son
        val memoryCardOptions = MemoryCardOptions("son", multiplex = multiplex)
        var sonCard = MemoryCard(options = memoryCardOptions)
        sonCard.load()
        sonCard.set("map", mapOf("a" to 1, "b" to 2))
        sonCard.set("list", listOf("1", "2", "3"))
        sonCard.save()
    }

    @Test
    fun testMultiplexLoad() {
        var parentCard = MemoryCard("parent")
        parentCard.load()

        val multiplex = Multiplex(parentCard, "parent")
        val memoryCardOptions = MemoryCardOptions("son", multiplex = multiplex)

        var sonCard = MemoryCard(options = memoryCardOptions)
        sonCard.load()
        sonCard.keys().forEach {
            println(it)
        }
        sonCard.values().forEach {
            println(it)
        }
    }

}
