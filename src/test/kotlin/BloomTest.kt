package index

import Address
import Bloom
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class BloomTest {
//    @Test
//    fun testInsertAddress() {
//        val bloom = Bloom()
//        val address = Address(ByteArray(20) { it.toByte() })
//        bloom.insertAddress(address)
//        assertTrue { bloom.blooms.isNotEmpty() }
//    }

    @Test
    fun testAddressToBits() {
        val bloom = Bloom()
        val address = Address(hexStringToByteArray("0371a82e4a9d0a4312f3ee2ac9c6958512891372"))
        val bits = bloom.addressToBits(address)
        assertTrue { bits.size == 5 }
        assertEquals(bits, listOf(108590u, 854595u, 257578u, 431493u, 594802u))
    }

    @Test
    fun testAddressToBits2() {
        val bloom = Bloom()
        val address = Address(hexStringToByteArray("3d493c51a916f86d6d1c04824b3a7431e61a3ca3"))
        val bits = bloom.addressToBits(address)
        assertTrue { bits.size == 5 }
        assertEquals(bits, listOf(605265u, 456813u, 787586u, 685105u, 670883u))
    }

    @Test
    fun testBloom() {
        val tests = listOf(
            TestData(
                Address(hexStringToByteArray("0371a82e4a9d0a4312f3ee2ac9c6958512891372")),
                listOf("0371a82e", "4a9d0a43", "12f3ee2a", "c9c69585", "12891372"),
                listOf(57780270u, 1251805763u, 317976106u, 3385234821u, 310973298u),
                listOf(108590u, 854595u, 257578u, 431493u, 594802u),
                true
            ),
            TestData(
                Address(hexStringToByteArray("3d493c51a916f86d6d1c04824b3a7431e61a3ca3")),
                listOf("3d493c51", "a916f86d", "6d1c0482", "4b3a7431", "e61a3ca3"),
                listOf(1028209745u, 2836854893u, 1830552706u, 1262122033u, 3860479139u),
                listOf(605265u, 456813u, 787586u, 685105u, 670883u),
                true
            ),
            TestData(
                Address(hexStringToByteArray("e1c15164dcfe79431f8421b5a311a829cf0907f3")),
                listOf("e1c15164", "dcfe7943", "1f8421b5", "a311a829", "cf0907f3"),
                listOf(3787542884u, 3707664707u, 528753077u, 2735843369u, 3473475571u),
                listOf(86372u, 948547u, 270773u, 108585u, 591859u),
                true
            ),
            TestData(
                Address(hexStringToByteArray("1296d3bb6dc0efbae431a12939fc15b2823db49b")),
                listOf("1296d3bb", "6dc0efba", "e431a129", "39fc15b2", "823db49b"),
                listOf(311874491u, 1841360826u, 3828457769u, 972821938u, 2185082011u),
                listOf(447419u, 61370u, 106793u, 791986u, 898203u),
                true
            ),
            TestData(
                Address(hexStringToByteArray("d09022c48298f268c2c431dadb9ca4c2534d9c1c")),
                listOf("d09022c4", "8298f268", "c2c431da", "db9ca4c2", "534d9c1c"),
                listOf(3499107012u, 2191061608u, 3267637722u, 3684476098u, 1397595164u),
                listOf(8900u, 586344u, 274906u, 828610u, 891932u),
                true
            ),
            TestData(
                Address(hexStringToByteArray("1296d3bb6dc0efbae431a12939fc15b2823db79b")),
                listOf("1296d3bb", "6dc0efba", "e431a129", "39fc15b2", "823db79b"),
                listOf(311874491u, 1841360826u, 3828457769u, 972821938u, 2185082779u),
                listOf(447419u, 61370u, 106793u, 791986u, 898971u),
                false
            ),
            TestData(
                Address(hexStringToByteArray("d09022c48298f268c2c431dadb9ca4c2534d9c1e")),
                listOf("d09022c4", "8298f268", "c2c431da", "db9ca4c2", "534d9c1e"),
                listOf(3499107012u, 2191061608u, 3267637722u, 3684476098u, 1397595166u),
                listOf(8900u, 586344u, 274906u, 828610u, 891934u),
                false
            ),
            TestData(
                Address(hexStringToByteArray("0341a82e4a9d0a4312f3ee2ac9c6958512891342")),
                listOf("0341a82e", "4a9d0a43", "12f3ee2a", "c9c69585", "12891342"),
                listOf(54634542u, 1251805763u, 317976106u, 3385234821u, 310973250u),
                listOf(108590u, 854595u, 257578u, 431493u, 594754u),
                false
            ),
            TestData(
                Address(hexStringToByteArray("3d493c51a916f86d6d1c04824b3a4431e61a3ca3")),
                listOf("3d493c51", "a916f86d", "6d1c0482", "4b3a4431", "e61a3ca3"),
                listOf(1028209745u, 2836854893u, 1830552706u, 1262109745u, 3860479139u),
                listOf(605265u, 456813u, 787586u, 672817u, 670883u),
                false
            ),
            TestData(
                Address(hexStringToByteArray("e1c15164dcfe49431f8421b5a311a829cf0904f3")),
                listOf("e1c15164", "dcfe4943", "1f8421b5", "a311a829", "cf0904f3"),
                listOf(3787542884u, 3707652419u, 528753077u, 2735843369u, 3473474803u),
                listOf(86372u, 936259u, 270773u, 108585u, 591091u),
                false
            )
        )

        val bloom = Bloom()
        for (tt in tests) {
            if (tt.insert) {
                bloom.insertAddress(tt.addr)
            }
        }

        val expectedLit = listOf(
            8900uL, 61370uL, 86372uL, 106793uL, 108585uL, 108590uL, 257578uL,
            270773uL, 274906uL, 431493uL, 447419uL, 456813uL, 586344uL, 591859uL,
            594802uL, 605265uL, 670883uL, 685105uL, 787586uL, 791986uL, 828610uL,
            854595uL, 891932uL, 898203uL, 948547uL
        )

        val stats = bloom.getStats()
        assertEquals(expectedLit.size, stats.bitsLit.size)
//        println(stats)

        assertEquals(expectedLit, stats.bitsLit)

        for (i in expectedLit.indices) {
            assertEquals(expectedLit[i], stats.bitsLit[i])
        }

        for (tt in tests) {
            if (tt.insert && !bloom.isMemberBytes(tt.addr)) {
                throw AssertionError("Address should be member, but isn't: ${tt.addr.bytes.toHexString()}")
            } else if (!tt.insert && bloom.isMemberBytes(tt.addr)) {
                throw AssertionError("Address should not be member, but is: ${tt.addr.bytes.toHexString()}")
            }
        }
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    data class TestData(
        val addr: Address,
        val parts: List<String>,
        val values: List<UInt>,
        val bits: List<UInt>,
        val insert: Boolean
    )
}
