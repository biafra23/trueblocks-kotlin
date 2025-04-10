import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val N_ADDR_LENGTH = 4
private const val N_APPS_LENGTH = 4
private const val MAGIC_LENGTH = 4
private const val HASH_LENGTH = 32
private const val HEADER_LENGTH = MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH + N_APPS_LENGTH
const val ADDR_LENGTH = 20
const val ADDR_OFFSET_LENGTH = 4
private const val ADDR_COUNT_LENGTH = 4
const val ADDR_RECORD_LENGTH = ADDR_LENGTH + ADDR_OFFSET_LENGTH + ADDR_COUNT_LENGTH
const val APP_BLOCKNUM_LENGTH = 4
const val APP_TX_INDEX_LENGTH = 4
const val APP_RECORD_LENGTH = APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH


@OptIn(ExperimentalUnsignedTypes::class)
class IndexParser {
    val addressRecords: HashMap<String, AddrRecord> = hashMapOf()

    fun parse(file: File) {
        val binaryData = file.readBytes()
        parse(binaryData)
    }

    fun parse(binaryData: ByteArray) {

        val nAddr = fourBytesToUInt(
            binaryData.sliceArray(
                IntRange(
                    MAGIC_LENGTH + HASH_LENGTH,
                    MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH - 1
                )
            ).toUByteArray()
        )
        val nApps = fourBytesToUInt(
            binaryData.sliceArray(IntRange(MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH, HEADER_LENGTH - 1))
                .toUByteArray()
        )
//        val magic = binaryData.sliceArray(IntRange(0, MAGIC_LENGTH - 1))
//        val hash = binaryData.sliceArray(IntRange(MAGIC_LENGTH, MAGIC_LENGTH + HASH_LENGTH - 1))
//        val header = Header(magic = magic, hash = hash, nAddr = nAddr, nApps = nApps)

        val appearanceTableStart = HEADER_LENGTH + (nAddr.toInt() * ADDR_RECORD_LENGTH)
        for (addressIndex in 1..<nAddr.toInt()) {
            val addressRecordBytes = binaryData.sliceArray(
                IntRange(
                    HEADER_LENGTH + (addressIndex * ADDR_RECORD_LENGTH),
                    HEADER_LENGTH + (addressIndex * ADDR_RECORD_LENGTH) + ADDR_RECORD_LENGTH - 1
                )
            )
            assert(addressRecordBytes.size == 28)

            val addressBytes = addressRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1))
            assert(addressBytes.size == 20)
            val addressString = addressBytes.joinToString("", prefix = "0x") { "%02x".format(it) }

            val appearancesOffset = fourBytesToUInt(
                addressRecordBytes.sliceArray(IntRange(ADDR_LENGTH, ADDR_LENGTH + ADDR_OFFSET_LENGTH - 1))
            )
            val appearancesCount = fourBytesToUInt(
                addressRecordBytes.sliceArray(IntRange(ADDR_LENGTH + ADDR_OFFSET_LENGTH, ADDR_RECORD_LENGTH - 1))
            )

            val appearances = mutableListOf<AppRecord>()
            for (countIndex in 0..<appearancesCount.toInt()) {
                val blockNum = fourBytesToUInt(
                    binaryData.sliceArray(
                        IntRange(
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + (appearancesOffset * APP_RECORD_LENGTH.toUInt()).toInt(),
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + (appearancesOffset * APP_RECORD_LENGTH.toUInt()).toInt() + APP_BLOCKNUM_LENGTH - 1
                        )
                    )
                )
                val txIndex = fourBytesToUInt(
                    binaryData.sliceArray(
                        IntRange(
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + (appearancesOffset * APP_RECORD_LENGTH.toUInt()).toInt() + APP_BLOCKNUM_LENGTH,
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + (appearancesOffset * APP_RECORD_LENGTH.toUInt()).toInt() + APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH - 1
                        )
                    )
                )
                appearances.add(AppRecord(blockNum, txIndex))
            }
            val addrRecord = AddrRecord(
                addressBytes,
                appearancesOffset,
                appearancesCount,
                appearances = appearances
            )
            addressRecords[addressString] = addrRecord
        }
    }

    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun fourBytesToUInt(bytes: UByteArray): UInt {
            assert(bytes.size == 4)
            val byteBuffer = ByteBuffer.wrap(bytes.toByteArray())
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

            // Convert the bytes to an unsigned integer (UInt)
            return byteBuffer.int.toUInt()
        }

        fun fourBytesToUInt(bytes: ByteArray): UInt {
            assert(bytes.size == 4)
            val byteBuffer = ByteBuffer.wrap(bytes)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

            // Convert the bytes to an unsigned integer (UInt)
            return byteBuffer.int.toUInt()
        }
    }
}

data class IndexFile(val header: Header, val addrRecords: List<AddrRecord>)
data class AddrRecord(
    var address: ByteArray,
    var offset: UInt,
    var count: UInt,
    var appearances: List<AppRecord>
) {
    override fun toString(): String {
        return "AddrRecord(address=${
            address.joinToString(
                "",
                prefix = "0x"
            ) { "%02x".format(it) }
        }, offset=$offset, count=$count), appearances: $appearances"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddrRecord

        if (!address.contentEquals(other.address)) return false
        if (offset != other.offset) return false
        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.contentHashCode()
        result = 31 * result + offset.hashCode()
        result = 31 * result + count.hashCode()
        return result
    }
}

data class AppRecord(
    var blockNumber: UInt,
    var txIndex: UInt
) {
    override fun toString(): String {
        return "AppRecord(blockNumber=$blockNumber (0x${blockNumber.toString(16)}), txIndex=$txIndex (0x${
            txIndex.toString(
                16
            )
        }))"
    }
}

data class Header(
    var magic: ByteArray,
    var hash: ByteArray,
    var nAddr: UInt,
    var nApps: UInt
) {
    override fun toString(): String {
        return "Header(magic=0x${magic.joinToString("") { "%02x".format(it) }}, hash=0x${
            hash.joinToString("") {
                "%02x".format(
                    it
                )
            }
        }, $nAddr, $nApps)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Header

        if (!magic.contentEquals(other.magic)) return false
        if (!hash.contentEquals(other.hash)) return false
        if (nAddr != other.nAddr) return false
        if (nApps != other.nApps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = magic.contentHashCode()
        result = 31 * result + hash.contentHashCode()
        result = 31 * result + nAddr.hashCode()
        result = 31 * result + nApps.hashCode()
        return result
    }
}
