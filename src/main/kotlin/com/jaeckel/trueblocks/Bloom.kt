import org.kethereum.model.Address
import org.komputing.khex.decode
import org.komputing.khex.encode
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

const val BLOOM_WIDTH_IN_BITS = 1048576
const val BLOOM_WIDTH_IN_BYTES = BLOOM_WIDTH_IN_BITS / 8
const val MAX_ADDRS_IN_BLOOM = 50000

data class BloomBytes(
    var nInserted: UInt = 0u,
    var bytes: ByteArray = ByteArray(BLOOM_WIDTH_IN_BYTES)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BloomBytes

        if (nInserted != other.nInserted) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nInserted.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class BloomHeader(
    var magic: UShort,
    var hash: String
)

class Bloom(
    var sizeOnDisc: Long = 0,
    var range: String = "undefined",  // The range of blocks this bloom filter covers
    var headerSize: Int = 34,
    var header: BloomHeader? = null,
    var count: UInt = 0u,
    var blooms: MutableList<BloomBytes> = mutableListOf()
) {
    companion object {

        fun parseBloomBytes(bytes: ByteArray, size: Long): Bloom {
            return parseBloomBytes(ByteArrayInputStream(bytes), size)
        }

        private fun parseBloomBytes(byteArrayInputStream: ByteArrayInputStream, size: Long): Bloom {
            val bloom = Bloom()
            bloom.sizeOnDisc = size
            bloom.range = "undefined" // can this be removed from Bloom or replaced with a true Range?
            bloom.header = byteArrayInputStream.readHeader()
            bloom.count = byteArrayInputStream.readUInt()

            bloom.blooms = MutableList(bloom.count.toInt()) {
                BloomBytes()
            }
            bloom.blooms.forEach {
                byteArrayInputStream.readBloomBytes(it)
            }

            return bloom
        }

        fun openBloom(file: File): Bloom {

            if (!file.exists()) {
                throw IllegalArgumentException("Required bloom file (${file.absolutePath}) missing")
            }

            val bloom = parseBloomBytes(file.readBytes(), file.length())
            return bloom
        }
    }

    fun insertAddress(addr: Address) {
        if (blooms.isEmpty()) {
            blooms.add(BloomBytes())
            count++
        }

        val loc = blooms.size - 1
        val bits = addressToBits(addr)

//        println(String.format("addressBits: %s %s %s %s %s", bits[0], bits[1], bits[2], bits[3], bits[4]))

        for (bit in bits) {
            val which = (bit / 8u).toInt()
            val whence = (bit % 8u).toInt()
            val index = BLOOM_WIDTH_IN_BYTES - which - 1
            val mask = (1 shl whence).toByte()
//            println("... which: " + which + ", whence: " + whence + ", index: " + index + ", mask: " + mask)
            blooms[loc].bytes[index] = blooms[loc].bytes[index] or mask
            //println("blooms[" + loc + "].bytes[" + index + "]: " + blooms[loc].bytes[index])
        }

        blooms[loc].nInserted++
        if (blooms[loc].nInserted > MAX_ADDRS_IN_BLOOM.toUInt()) {
            blooms.add(BloomBytes())
            count++
        }
    }

    fun addressToBits(addr: Address): List<UInt> {
        val slice = decode(addr.hex)
        if (slice.size != 20) {
            throw IllegalArgumentException("Invalid address length")
        }

        val bits = mutableListOf<UInt>()
        for (i in slice.indices step 4) {
            val bytes = slice.copyOfRange(i, i + 4)
            val bit = (ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).int.toUInt() % BLOOM_WIDTH_IN_BITS.toUInt())
            bits.add(bit)
        }
        return bits
    }

    fun getStats(): BloomStats {
        var nBlooms = 0uL
        var nInserted = 0uL
        var nBitsLit = 0uL
        var nBitsNotLit = 0uL
        var sz = 4uL
        val bitsLit = mutableListOf<ULong>()

        nBlooms = count.toULong()
        for (bf in blooms) {
            nInserted += bf.nInserted.toULong()
            sz += 4uL + bf.bytes.size.toULong()
            for (bitPos in 0..<bf.bytes.size * 8) {
                val tester = BitChecker(bit = bitPos.toUInt(), bytes = bf.bytes)
                if (isBitLit(tester)) {
                    nBitsLit++
                    bitsLit.add(bitPos.toULong())
                } else {
                    nBitsNotLit++
                }
            }
        }
//        println(String.format("   nBitsLit: %s", nBitsLit))
//        println(String.format("nBitsNotLit: %s", nBitsNotLit))

        return BloomStats(nBlooms, nInserted, nBitsLit, nBitsNotLit, sz, bitsLit)
    }

    fun isMemberBytes(addr: Address): Boolean {
        val whichBits = addressToBits(addr)
        for (bb in blooms) {
            val tester = BitChecker(whichBits = whichBits, bytes = bb.bytes)
            if (isMember(tester)) {
                return true
            }
        }
        return false
    }

    private fun isBitLit(tester: BitChecker): Boolean {

        val which = tester.bit.toInt() / 8
        val index = BLOOM_WIDTH_IN_BYTES - which - 1
        val whence = tester.bit.toInt() % 8

        val byt = tester.bytes[index]
        val mask = (1 shl whence).toByte()
        val res = byt and mask

        return res != 0.toByte()
    }

    fun isMember(tester: BitChecker): Boolean {
        for (bit in tester.whichBits) {
            if (!isBitLit(BitChecker(bit = bit, bytes = tester.bytes))) {
                return false
            }
        }
        return true
    }
}

fun ByteArrayInputStream.readHeader(): BloomHeader {
    val magic = readUShort()
    val buffer = ByteArray(32)
    read(buffer)
    val hash = encode(buffer)
    return BloomHeader(magic = magic, hash = hash)
}

fun ByteArrayInputStream.readBloomBytes(bloomBytes: BloomBytes) {
    bloomBytes.nInserted = readUInt()
    val buffer = ByteArray(BLOOM_WIDTH_IN_BYTES)
    read(buffer)
    bloomBytes.bytes = buffer
}

fun ByteArrayInputStream.readUShort(): UShort {
    val buffer = ByteArray(2)
    read(buffer)
    return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).short.toUShort()
}

fun ByteArrayInputStream.readUInt(): UInt {
    val buffer = ByteArray(4)
    read(buffer)
    return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
}

data class BloomStats(
    val nBlooms: ULong,
    val nInserted: ULong,
    val nBitsLit: ULong,
    val nBitsNotLit: ULong,
    val sz: ULong,
    val bitsLit: List<ULong>
)

data class BitChecker(
    val whichBits: List<UInt> = listOf(),
    val offset: UInt = 0u,
    val bit: UInt = 0u,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitChecker

        if (whichBits != other.whichBits) return false
        if (offset != other.offset) return false
        if (bit != other.bit) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = whichBits.hashCode()
        result = 31 * result + offset.hashCode()
        result = 31 * result + bit.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
