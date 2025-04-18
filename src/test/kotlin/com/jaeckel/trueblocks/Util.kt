package com.jaeckel.trueblocks

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class Util {

    companion object {
        fun loadResourceFile(fileName: String): File {
            val resource = javaClass.getResource("/$fileName")
            return File(resource.toURI())
        }
    }

    @Test
    fun testLoadResourceFile() {
        val file = loadResourceFile("007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index")
        assertContains(file.absolutePath, "007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index")
        assertTrue { file.exists() }
    }
}
