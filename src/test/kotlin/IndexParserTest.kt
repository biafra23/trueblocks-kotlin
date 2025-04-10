import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertNotNull

class IndexParserTest {

    @Test
    fun parseToAddressRecords() {
        val indexParser =
            IndexParser(Util.loadResourceFile("007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))
        indexParser.parseToAddressRecords()

        val addressRecord = indexParser.addressRecords["0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"]
        assertNotNull(addressRecord)

        assertEquals(1, addressRecord.appearances.size)
        assertEquals(7799984u, addressRecord.appearances[0].blockNumber)
        assertEquals(33u, addressRecord.appearances[0].txIndex)
    }

    @Test
    fun parseToAddressRecords2() {
        val ipfsHttpClient = IpfsHttpClient()
        val addressToCheck = "0xfffc3ead0df70e9bbe805af463814c2e6de5ae79".lowercase()
        val addressRecords =
            ipfsHttpClient.fetchIndex("QmckY96hpZ1yzC53gx4t7UXHicoqRE2UwwDi33v9aNBqGq")?.addressRecords
        assertNotNull(addressRecords)

        val addrRecord = addressRecords[addressToCheck]
        assertNotNull(addrRecord)
        assertEquals(1, addrRecord.appearances.size)
        assertEquals(7881824u, addrRecord.appearances[0].blockNumber)
        assertEquals(52u, addrRecord.appearances[0].txIndex)
    }

    @Test
    fun testIndexParseToAddressRecords() {

        val ipfsHttpClient = IpfsHttpClient()

        val addressToCheck = "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()
        val addrRecords =
            ipfsHttpClient.fetchIndex("QmUf5JhHUHjdg5TkfC3ieL1Jcw5cfnpmYhVXxfmLmA9xPG")?.addressRecords //019732529-019735044

        assertNotNull(addrRecords)
        val addrRecord = addrRecords[addressToCheck]

        assertNotNull(addrRecord)
        assertEquals(1u, addrRecord.count)
        assertEquals(1, addrRecord.appearances.size)
        assertEquals(19733118u, addrRecord.appearances[0].blockNumber)
        assertEquals(49u, addrRecord.appearances[0].txIndex)
    }

    @Test
    fun testIndexParseToAddressRecords2() {

        val ipfsHttpClient = IpfsHttpClient()

        val addressToCheck = "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()
//        val addressToCheck = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".lowercase()
//        val addressToCheck = "0x35CF415F7D9a50237ae580Fc0591960b5a5a3E6F".lowercase()
        val addressRecords =
            ipfsHttpClient.fetchIndex("QmXW7o1MhytrittDfNFQnuxTvUxhJGghjeTjmR7xqeMBRA")?.addressRecords // 019997745-020000000

        assertNotNull(addressRecords)
        val addressRecord = addressRecords[addressToCheck]

        assertNotNull(addressRecord)
        assertEquals(1u, addressRecord.count)
        assertEquals(1, addressRecord.appearances.size)
        assertEquals(19999578u, addressRecord.appearances[0].blockNumber)
        assertEquals(170u, addressRecord.appearances[0].txIndex)
    }

    @Test
    fun testIndexParseToAddressRecords3() {

        val ipfsHttpClient = IpfsHttpClient()

        val addressToCheck = "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()
//        val addrRecords = ipfsHttpClient.fetchIndex("QmTMRnGkEPa5kUQBmVSd2baefSDGabZp6bfzft3Nwjt1rs")?.addressRecords // 018867202-018869787
        val addrRecords =
            ipfsHttpClient.fetchIndex("QmZgMXcP2V9dp8Jk11vJPmLGdDxsxD4QrNiC4JndBoPAkT")?.addressRecords // 018864495-018867201

        assertNotNull(addrRecords)
        val addrRecord = addrRecords[addressToCheck]

        assertNotNull(addrRecord)
        assertEquals(2u, addrRecord.count)
        assertEquals(2, addrRecord.appearances.size)
        assertEquals(18867145u, addrRecord.appearances[0].blockNumber)
        assertEquals(152u, addrRecord.appearances[0].txIndex)
        assertEquals(18867170u, addrRecord.appearances[1].blockNumber)
        assertEquals(41u, addrRecord.appearances[1].txIndex)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun fourBytesToUInt() {
        val result = IndexParser.fourBytesToUInt(ubyteArrayOf(0x01u, 0x02u, 0xffu, 0xffu))

        kotlin.test.assertEquals(4_294_902_273u, result)
    }

    @Test
    fun testFindAppearances() {
        val indexParser =
            IndexParser(Util.loadResourceFile("007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))

        val appearances = indexParser.findAppearances("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79")

        assertNotNull(appearances)
        assertEquals(1, appearances.size)
        assertEquals(7799984u, appearances[0].blockNumber)
        assertEquals(33u, appearances[0].txIndex)
    }

    @Test
    fun testFindAppearances2() {

        val ipfsHttpClient = IpfsHttpClient()
        val appearances =
            ipfsHttpClient.fetchIndex("QmZgMXcP2V9dp8Jk11vJPmLGdDxsxD4QrNiC4JndBoPAkT", false)
                ?.findAppearances("0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase())

        assertNotNull(appearances)
        assertEquals(2, appearances.size)
        assertEquals(18867145u, appearances[0].blockNumber)
        assertEquals(152u, appearances[0].txIndex)
        assertEquals(18867170u, appearances[1].blockNumber)
        assertEquals(41u, appearances[1].txIndex)
    }

    @Test
    fun testShouldNotFindAppearance() {
        // Regression for bug in binarySearch
        val ipfsHttpClient = IpfsHttpClient()
        val appearances =
            ipfsHttpClient.fetchIndex("QmPoU3zot1LNTUYomGER3ykLJZGcxPui8g17WQu9a3zsQA", false)
                ?.findAppearances("0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()) // Block Range: 002683121-002690830

        assertNotNull(appearances)
        assertEquals(0, appearances.size)

    }

    @Test
    fun testShouldNotFindAppearance2() {
        // Regression for bug in binarySearch
        val ipfsHttpClient = IpfsHttpClient()
        val appearances =
            ipfsHttpClient.fetchIndex("QmR9HbMRygR1UDjd6FLeR4UDWM3MrWjSxBNSdRY1pLKVvQ", false)
                ?.findAppearances("0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()) // Block Range: 007440360-007446883

        assertNotNull(appearances)
        assertEquals(0, appearances.size)
    }
}
