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

fun openBloom(path: String): Bloom {
    println("path: $path")
    val bloom = Bloom()
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

fun addressToBits(address: ByteArray): IntArray {
    require(address.size == 20) { "Invalid address length." }

    val bits = IntArray(5)
    for (i in 0 until 5) {
        val segment = ByteBuffer.wrap(address.copyOfRange(i * 4, i * 4 + 4)).order(ByteOrder.BIG_ENDIAN).int
        bits[i] = segment % BLOOM_WIDTH_IN_BITS
    }
    return bits
}

fun main() {
    // Example usage
//    val bloom = openBloom("path/to/bloom/file")
//    val bloom = openBloom("/Users/biafra/TrueblocksParser/QmR5XPnYuJuypCu8LzbWKki3i1DnfWZsgJF9JsqPWL2hCF.bloom")
//    val bloom = openBloom("/Users/biafra/TrueblocksParser/QmQfn7HkkyjiipBMYvnoQExp7G26NVv17a1pJZyPGpVuf6.bloom")
    val bloom = openBloom("/Users/biafra/TrueblocksParser/QmR5XPnYuJuypCu8LzbWKki3i1DnfWZsgJF9JsqPWL2hCF.bloom")
    val address = ByteArray(20) // Example address
    //val hexString = "0x1234567890abcdef1234567890abcdef12345678"
    val hexString = "0xf95de6da218d9bf49626c04679d13a03b2c394ca" // 20783805 // 20782958
    val byteArray = hexStringToByteArray(hexString)
    println(byteArray.joinToString(" ") { "%02x".format(it) })
    //bloom.insertAddress(address)

    val isHit = bloom.isAddressInFilter(address)

    println("Address is in filter: $isHit")
}

fun hexStringToByteArray(hex: String): ByteArray {
    val cleanHex = if (hex.startsWith("0x")) hex.substring(2) else hex
    require(cleanHex.length % 2 == 0) { "Invalid hex string length." }

    return ByteArray(cleanHex.length / 2) { i ->
        ((Character.digit(cleanHex[i * 2], 16) shl 4) + Character.digit(cleanHex[i * 2 + 1], 16)).toByte()
    }
}

fun Bloom.isAddressInFilter(address: ByteArray): Boolean {
    val bits = addressToBits(address)

    for (bloomBytes in blooms) {
        println("using filter ${bloomBytes.nInserted}")
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
