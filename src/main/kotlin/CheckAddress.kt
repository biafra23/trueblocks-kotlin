import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

const val BLOOM_WIDTH_IN_BITS = 1048576
const val BLOOM_WIDTH_IN_BYTES = BLOOM_WIDTH_IN_BITS / 8
const val MAX_ADDRS_IN_BLOOM = 50000

data class BloomBytes(var nInserted: Int = 0, val bytes: ByteArray = ByteArray(BLOOM_WIDTH_IN_BYTES))

data class Bloom(
    var file: RandomAccessFile? = null,
    var count: UInt = 0u,
    val blooms: MutableList<BloomBytes> = mutableListOf()
)

class CheckAddress {

    val bloom = Bloom()
    fun openBloom(path: String): Bloom {
        println("path: $path")
        val file = File(path)
        if (!file.exists()) throw IllegalArgumentException("Required bloom file ($path) missing")
        bloom.file = RandomAccessFile(file, "r")
        bloom.file!!.seek(34) // Skip the first 34 bytes (magic + hash)
        val countBytes = ByteArray(4)
        bloom.file!!.readFully(countBytes)
        bloom.count = ByteBuffer.wrap(countBytes).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
        println("Number of filters in file: ${bloom.count}")

        for (i in 0 until bloom.count.toInt()) {
            val bloomBytes = BloomBytes()
            bloom.file!!.readFully(bloomBytes.bytes)
            bloom.blooms.add(bloomBytes)
        }

        return bloom
    }


    fun main(hexString: String, bloomFile: String) {
        val bloom = openBloom(bloomFile)
        val byteArray = hexStringToByteArray(hexString)
        val address = Address(byteArray)

//        bloom.insertAddress(byteArray)

//        val isHit = bloom.isAddressInFilter(byteArray)
//        println("---> Address (${byteArray.joinToString(" ") { "%02x".format(it) }}) is in filter: $isHit")

        val isMember = isMember(address)
        println("\n---> Address (${byteArray.joinToString(" ") { "%02x".format(it) }}) is in filter: $isMember")
    }

    fun hexStringToByteArray(hex: String): ByteArray {
        val cleanHex = if (hex.startsWith("0x")) hex.substring(2) else hex
        require(cleanHex.length % 2 == 0) { "Invalid hex string length." }

        return ByteArray(cleanHex.length / 2) { i ->
            ((Character.digit(cleanHex[i * 2], 16) shl 4) + Character.digit(cleanHex[i * 2 + 1], 16)).toByte()
        }
    }

    fun isMember(addr: Address): Boolean {
        val whichBits = addressToBits(addr.bytes)
        val headerSize = 38
        val count = bloom.count
        var offset = headerSize + 4 // the end of Count
        for (j in 0 until count.toInt()) {

            offset += 4 // Skip over NInserted
            val tester = BitChecker(offset, whichBits)
            if (isMember(tester)) {
                println("return true (1)")
                return true
            }
            offset += BLOOM_WIDTH_IN_BYTES
        }
        println("return false (2)")
        return false
    }

    private fun isMember(tester: BitChecker): Boolean {
        for (bit in tester.whichBits) {
            tester.bit = bit
            if (!isBitLit(tester)) {
                println("return false (3)")
                return false
            }
        }
        println("return true (4)")
        return true
    }

    private fun isBitLit(tester: BitChecker): Boolean {
        val which = tester.bit / 8
        val index = BLOOM_WIDTH_IN_BYTES - which - 1
        val whence = tester.bit % 8
        val mask = (1 shl whence).toByte()

        val res: Byte
        if (tester.bytes != null) {
            // In some cases, we've already read the bytes into memory, so use them if they're here
            val byt = tester.bytes!![index.toInt()]
            res = (byt.toInt() and mask.toInt()).toByte()
        } else {
            bloom.file!!.seek((tester.offset + index).toLong())
            val byt = bloom.file!!.readByte()
            res = (byt.toInt() and mask.toInt()).toByte()
        }

        return res.toInt() != 0
    }
}

fun addressToBits(address: ByteArray): IntArray {
    require(address.size == 20) { "invalid address length" }
    address.forEach { byte ->
        print(byte.toInt().and(0xFF).toString(2).padStart(32, '0'))
        print(" ")
    }
    val bits = IntArray(5)
    for (i in 0 until 20 step 4) {
        val bytes = address.copyOfRange(i, i + 4)
        val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val segment = byteBuffer.int.toLong() and 0xFFFFFFFFL
        println("i:$i, mod: $BLOOM_WIDTH_IN_BITS, binary: $segment, bits/4: ${(segment % BLOOM_WIDTH_IN_BITS).toInt()}")
        bits[i / 4] = (segment % BLOOM_WIDTH_IN_BITS).toInt()
        println("bits: ${bits.forEach { print("$it ") }}")
    }

    // Print the bits in a readable manner
    println("Bloom bits:")
    bits.forEach { integerValue ->
        print(integerValue.toString(2).padStart(32, '0'))
        print(" ")
    }

    return bits
}

fun Bloom.isAddressInFilter(address: ByteArray): Boolean {
    val bits = addressToBits(address)
    println("blooms#: ${blooms.size}")
    for (bloomBytes in blooms) {
        var isHit = true
        for (bit in bits) {
            val which = bit / 8
            val whence = bit % 8
            val index = BLOOM_WIDTH_IN_BYTES - which - 1
            val mask = (1 shl whence).toByte()
            if ((bloomBytes.bytes[index] and mask) == 0.toByte()) {
                isHit = false
                break
            }
        }
        if (isHit) {
            return true
        }
    }
    return false
}

fun Bloom.insertAddress(address: ByteArray) {
    if (blooms.isEmpty()) {
        blooms.add(BloomBytes())
        count++
    }

    val bits = addressToBits(address)
    val loc = blooms.size - 1

    for (bit in bits) {
        val which = bit / 8
        val whence = bit % 8
        val index = BLOOM_WIDTH_IN_BYTES - which - 1
        val mask = (1 shl whence).toByte()
        blooms[loc].bytes[index] = blooms[loc].bytes[index] or mask
    }

    blooms[loc].nInserted++
    if (blooms[loc].nInserted > MAX_ADDRS_IN_BLOOM) {
        blooms.add(BloomBytes())
        count++
    }
}

class BloomIsMember(private val file: RandomAccessFile, private val headerSize: Int, private val count: Int) {


    private fun addressToBits(addr: Address): IntArray {
        val slice = addr.bytes
        if (slice.size != 20) {
            throw IllegalArgumentException("Invalid address length.")
        }

        val bits = IntArray(5)
        for (i in slice.indices step 4) {
            val bytes = slice.copyOfRange(i, i + 4)
            val res = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).int % BLOOM_WIDTH_IN_BITS
            bits[i / 4] = res
        }
        return bits
    }

    companion object {
        const val BLOOM_WIDTH_IN_BYTES = 256
        const val BLOOM_WIDTH_IN_BITS = BLOOM_WIDTH_IN_BYTES * 8
    }
}

data class Address(val bytes: ByteArray)

class BitChecker(val offset: Int, val whichBits: IntArray) {
    var bit: Int = 0
    var bytes: ByteArray? = null
}
