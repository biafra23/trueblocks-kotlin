//import java.io.File
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//
//private const val N_ADDR_LENGTH = 4
//private const val N_APPS_LENGTH = 4
//private const val MAGIC_LENGTH = 4
//private const val HASH_LENGTH = 32
//private const val HEADER_LENGTH = MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH + N_APPS_LENGTH
//private const val ADDR_LENGTH = 20
//private const val ADDR_OFFSET_LENGTH = 4
//private const val ADDR_COUNT_LENGTH = 4
//private const val ADDR_RECORD_LENGTH = ADDR_LENGTH + ADDR_OFFSET_LENGTH + ADDR_COUNT_LENGTH
//private const val APP_BLOCKNUM_LENGTH = 4
//private const val APP_TX_INDEX_LENGTH = 4
//private const val APP_RECORD_LENGTH = APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH
//
//@OptIn(ExperimentalUnsignedTypes::class)
//class IndexParser {
//    fun parse(fileName: File) {
//        // Read the binary data into a ByteArray
//        val binaryData = fileName.readBytes()
//
//        val nAddr = fourBytesToUInt(
//            binaryData.sliceArray(
//                IntRange(
//                    MAGIC_LENGTH + HASH_LENGTH,
//                    MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH
//                )
//            ).toUByteArray()
//        )
//        val nApps = fourBytesToUInt(
//            binaryData.sliceArray(IntRange(MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH, HEADER_LENGTH)).toUByteArray()
//        )
//        val magic = binaryData.sliceArray(IntRange(0, MAGIC_LENGTH - 1))
//        val hash = binaryData.sliceArray(IntRange(MAGIC_LENGTH, MAGIC_LENGTH + HASH_LENGTH))
//        val header = Header(magic = magic, hash = hash, nAddr = nAddr, nApps = nApps)
//
//        println("header: $header")
//
//        val appearanceTableStart = HEADER_LENGTH + (nAddr.toInt() * ADDR_RECORD_LENGTH)
//        println("appearanceTableStart: $appearanceTableStart")
//        var appOffset = 0
//        for (addrIndex in 1..<nAddr.toInt()) {
//            val addrRecordBytes = binaryData.sliceArray(
//                IntRange(
//                    HEADER_LENGTH + (addrIndex * ADDR_RECORD_LENGTH),
//                    HEADER_LENGTH + (addrIndex * ADDR_RECORD_LENGTH) + ADDR_RECORD_LENGTH
//                )
//            )
//            val addr =
//                "0x${addrRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1)).joinToString("") { "%02x".format(it) }}"
//            val offset = fourBytesToUInt(
//                addrRecordBytes.sliceArray(IntRange(ADDR_LENGTH, ADDR_LENGTH + ADDR_OFFSET_LENGTH)).toUByteArray()
//            )
//            val count = fourBytesToUInt(
//                addrRecordBytes.sliceArray(IntRange(ADDR_LENGTH + ADDR_OFFSET_LENGTH, ADDR_RECORD_LENGTH))
//                    .toUByteArray()
//            )
//
//
//            appOffset += offset.toInt() * APP_RECORD_LENGTH
//            val appearances = mutableListOf<AppRecord>()
//            for (countIndex in 0..<count.toInt()) {
//                val blockNum = fourBytesToUInt(
//                    binaryData.sliceArray(
//                        IntRange(
//                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH),
//                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH
//                        )
//                    ).toUByteArray()
//                )
//                val txIndex = fourBytesToUInt(
//                    binaryData.sliceArray(
//                        IntRange(
//                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH,
//                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH
//                        )
//                    ).toUByteArray()
//                )
//
//                appearances.add(AppRecord(blockNum, txIndex))
//                //println("----> block_num: $blockNum, tx_index: $txIndex")
//            }
//            val addrRecord = AddrRecord(
//                addrRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1)),
//                offset,
//                count,
//                appearances = appearances
//            )
//            println("addrRecord: $addrRecord")
//
//        }
//
//        println("Binary data read from file: ${binaryData.take(4).joinToString(" ") { "%02x".format(it) }}")
//
//    }
//
//    @OptIn(ExperimentalUnsignedTypes::class)
//    fun fourBytesToUInt(bytes: UByteArray): UInt {
//        val byteBuffer = ByteBuffer.wrap(bytes.toByteArray())
//        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
//
//        // Convert the bytes to an unsigned integer (UInt)
//        return byteBuffer.int.toUInt()
//    }
//}
//
//data class IndexFile(val header: Header, val addrRecords: List<AddrRecord>)
//data class AddrRecord(
//    var address: ByteArray,
//    var offset: UInt,
//    var count: UInt,
//    var appearances: List<AppRecord>
//) {
//    override fun toString(): String {
//        return "AddrRecord(address=0x${address.joinToString("") { "%02x".format(it) }}, offset=$offset, count=$count), appearances: $appearances"
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as AddrRecord
//
//        if (!address.contentEquals(other.address)) return false
//        if (offset != other.offset) return false
//        if (count != other.count) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = address.contentHashCode()
//        result = 31 * result + offset.hashCode()
//        result = 31 * result + count.hashCode()
//        return result
//    }
//}
//
//data class AppRecord(
//    var blockNumber: UInt,
//    var txIndex: UInt
//)
//
//data class Header(
//    var magic: ByteArray,
//    var hash: ByteArray,
//    var nAddr: UInt,
//    var nApps: UInt
//) {
//    override fun toString(): String {
//        return "Header(magic=0x${magic.joinToString("") { "%02x".format(it) }}, hash=0x${
//            hash.joinToString("") {
//                "%02x".format(
//                    it
//                )
//            }
//        }, $nAddr, $nApps)"
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Header
//
//        if (!magic.contentEquals(other.magic)) return false
//        if (!hash.contentEquals(other.hash)) return false
//        if (nAddr != other.nAddr) return false
//        if (nApps != other.nApps) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = magic.contentHashCode()
//        result = 31 * result + hash.contentHashCode()
//        result = 31 * result + nAddr.hashCode()
//        result = 31 * result + nApps.hashCode()
//        return result
//    }
//}
