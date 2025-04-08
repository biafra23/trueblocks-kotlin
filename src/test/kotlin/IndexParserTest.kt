import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertNotNull

class IndexParserTest {

    @Test
    fun parse() {
        val indexParser = IndexParser()
        indexParser.parse(Util.loadResourceFile("007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))

        val addressRecord = indexParser.addressRecords["0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"]
        assertNotNull(addressRecord)
        assertEquals(1, addressRecord.appearances.size)
        assertEquals(7799463u, addressRecord.appearances[0].blockNumber)
        assertEquals(3u, addressRecord.appearances[0].txIndex)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun fourBytesToUInt() {
        val result = IndexParser.fourBytesToUInt(ubyteArrayOf(0x01u, 0x02u, 0xffu, 0xffu))

        kotlin.test.assertEquals(4_294_902_273u, result)
    }
}
