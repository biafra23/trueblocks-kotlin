import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val MAGIC_LENGTH = 2
private const val COUNT_LENGTH = 4
private const val HASH_LENGTH = 32
private const val HEADER_LENGTH = MAGIC_LENGTH + HASH_LENGTH + COUNT_LENGTH
private const val BLOOM_FILTER_LENGTH = 200000

class BloomParser {

    fun parse(fileName: File) {
        val binaryData = fileName.readBytes()
        parse(binaryData)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun parse(binaryData: ByteArray) {
        val count = bytesToUInt(
            binaryData.sliceArray(
                IntRange(
                    MAGIC_LENGTH + HASH_LENGTH,
                    MAGIC_LENGTH + HASH_LENGTH + COUNT_LENGTH
                )
            ).toUByteArray()
        )

        println("count: $count")

        val nApps = bytesToUInt(
            binaryData.sliceArray(IntRange(MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH, HEADER_LENGTH)).toUByteArray()
        )
        val magic = binaryData.sliceArray(IntRange(0, MAGIC_LENGTH - 1))
        val hash = binaryData.sliceArray(IntRange(MAGIC_LENGTH, MAGIC_LENGTH + HASH_LENGTH))
        val header = Header(magic = magic, hash = hash, nAddr = count, nApps = nApps)

        println("header: $header")

        val appearanceTableStart = HEADER_LENGTH + (count.toInt() * ADDR_RECORD_LENGTH)
        println("appearanceTableStart: $appearanceTableStart")
        var appOffset = 0
        for (addrIndex in 1..<count.toInt()) {
            val addrRecordBytes = binaryData.sliceArray(
                IntRange(
                    HEADER_LENGTH + (addrIndex * ADDR_RECORD_LENGTH),
                    HEADER_LENGTH + (addrIndex * ADDR_RECORD_LENGTH) + ADDR_RECORD_LENGTH
                )
            )
//            val addr = "0x${addrRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1)).joinToString("") { "%02x".format(it) }}"
            val offset = bytesToUInt(
                addrRecordBytes.sliceArray(IntRange(ADDR_LENGTH, ADDR_LENGTH + ADDR_OFFSET_LENGTH)).toUByteArray()
            )
            val count = bytesToUInt(
                addrRecordBytes.sliceArray(IntRange(ADDR_LENGTH + ADDR_OFFSET_LENGTH, ADDR_RECORD_LENGTH))
                    .toUByteArray()
            )


            appOffset += offset.toInt() * APP_RECORD_LENGTH
            val appearances = mutableListOf<AppearanceRecord>()
            for (countIndex in 0..<count.toInt()) {
                val blockNum = bytesToUInt(
                    binaryData.sliceArray(
                        IntRange(
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH),
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH
                        )
                    ).toUByteArray()
                )
                val txIndex = bytesToUInt(
                    binaryData.sliceArray(
                        IntRange(
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH,
                            appearanceTableStart + (countIndex * APP_RECORD_LENGTH) + APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH
                        )
                    ).toUByteArray()
                )

                appearances.add(AppearanceRecord(blockNum, txIndex))
                //println("----> block_num: $blockNum, tx_index: $txIndex")
            }
            val addressRecord = AddressRecord(
                addrRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1)),
                offset,
                count,
                appearances = appearances
            )
            println("addrRecord: $addressRecord")

        }

        println("Binary data: ${binaryData.take(4).joinToString(" ") { "%02x".format(it) }}")

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun bytesToUInt(bytes: UByteArray): UInt {
        val byteBuffer = ByteBuffer.wrap(bytes.toByteArray())
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        // Convert the bytes to an unsigned integer (UInt)
        return byteBuffer.int.toUInt()
    }
}
