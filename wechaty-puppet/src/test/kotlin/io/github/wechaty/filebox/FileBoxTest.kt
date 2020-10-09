package io.github.wechaty.filebox

import junit.framework.Assert.assertEquals
import org.junit.Test

const val EXPECTED_FILEBOX_URL = "http://testurl"
const val EXPECTED_FILEBOX_NAME = "fileboxname"

class FileBoxTest {

    @Test
    fun testFileBoxFromURLShallHaveCorrectNameAndURL() {

        var filebox : FileBox = FileBox.fromJson("{\"remoteUrl\":\"" + EXPECTED_FILEBOX_URL + "\"," +
            "\"name\":\"" + EXPECTED_FILEBOX_NAME + "\"," +
            "\"boxType\":2}")

        assertEquals(EXPECTED_FILEBOX_URL, filebox.remoteUrl)
        assertEquals(EXPECTED_FILEBOX_NAME, filebox.name)
    }
}
