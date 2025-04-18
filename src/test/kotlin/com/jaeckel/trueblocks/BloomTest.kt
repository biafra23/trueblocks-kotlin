package com.jaeckel.trueblocks

import com.jaeckel.trueblocks.Util.Companion.loadResourceFile
import org.kethereum.model.Address
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BloomTest {

    // tests insertAddress, isBitLit, getStats, addressToBits and isMemberBytes
    @Test
    fun testBloom() {
        val tests = listOf(
            TestData(
                Address("0371a82e4a9d0a4312f3ee2ac9c6958512891372"),
                listOf(108590u, 854595u, 257578u, 431493u, 594802u),
                true
            ),
            TestData(
                Address("3d493c51a916f86d6d1c04824b3a7431e61a3ca3"),
                listOf(605265u, 456813u, 787586u, 685105u, 670883u),
                true
            ),
            TestData(
                Address("e1c15164dcfe79431f8421b5a311a829cf0907f3"),
                listOf(86372u, 948547u, 270773u, 108585u, 591859u),
                true
            ),
            TestData(
                Address("1296d3bb6dc0efbae431a12939fc15b2823db49b"),
                listOf(447419u, 61370u, 106793u, 791986u, 898203u),
                true
            ),
            TestData(
                Address("d09022c48298f268c2c431dadb9ca4c2534d9c1c"),
                listOf(8900u, 586344u, 274906u, 828610u, 891932u),
                true
            ),
            TestData(
                Address("1296d3bb6dc0efbae431a12939fc15b2823db79b"),
                listOf(447419u, 61370u, 106793u, 791986u, 898971u),
                false
            ),
            TestData(
                Address("d09022c48298f268c2c431dadb9ca4c2534d9c1e"),
                listOf(8900u, 586344u, 274906u, 828610u, 891934u),
                false
            ),
            TestData(
                Address("0341a82e4a9d0a4312f3ee2ac9c6958512891342"),
                listOf(108590u, 854595u, 257578u, 431493u, 594754u),
                false
            ),
            TestData(
                Address("3d493c51a916f86d6d1c04824b3a4431e61a3ca3"),
                listOf(605265u, 456813u, 787586u, 672817u, 670883u),
                false
            ),
            TestData(
                Address("e1c15164dcfe49431f8421b5a311a829cf0904f3"),
                listOf(86372u, 936259u, 270773u, 108585u, 591091u),
                false
            )
        )

        val bloom = Bloom()
        for (tt in tests) {
            if (tt.insert) {
                bloom.insertAddress(tt.addr)
            }
            assertEquals(tt.bits, bloom.addressToBits(tt.addr)) // tests addressToBits
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
                throw AssertionError("Address should be member, but isn't: ${tt.addr.hex}")
            } else if (!tt.insert && bloom.isMemberBytes(tt.addr)) {
                throw AssertionError("Address should not be member, but is: ${tt.addr.hex}")
            }
        }
    }

    @Test
    fun testOpenBloom() {
        val bloomFileName = "007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom"
        val addressString = "0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"

        val bloom = Bloom.openBloom(loadResourceFile(bloomFileName))
        val isMember = bloom.isMemberBytes(Address(addressString))
        assertTrue(isMember)
    }

    @Test
    fun testOpenBloom2() {
        val bloomFileName = "QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE"
        val addressString = "0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"

        val bloom = Bloom.openBloom(loadResourceFile(bloomFileName))
        val isMember = bloom.isMemberBytes(Address(addressString))
        assertTrue(isMember)
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    data class TestData(
        val addr: Address,
        val bits: List<UInt>,
        val insert: Boolean
    )
}

