import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class IndexParserTest {

    @Test
    fun parse() {
        val indexParser = IndexParser()
        indexParser.parse(Util.loadResourceFile("007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))

        // assert....
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun fourBytesToUInt() {
        val result = IndexParser.fourBytesToUInt(ubyteArrayOf(0x01u, 0x02u, 0xffu, 0xffu))

        kotlin.test.assertEquals(4_294_902_273u, result)
    }
}
