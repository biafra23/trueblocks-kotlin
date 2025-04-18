package com.jaeckel.trueblocks

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
class IndexParser(val binaryData: ByteArray) {

    constructor(filename: File) : this(filename.readBytes())
    constructor(filename: String) : this(File(filename).readBytes())

    val addressRecords: HashMap<String, AddressRecord> = hashMapOf()
    val header: Header

    init {
        val magic = binaryData.sliceArray(IntRange(0, MAGIC_LENGTH - 1))
        val hash = binaryData.sliceArray(IntRange(MAGIC_LENGTH, MAGIC_LENGTH + HASH_LENGTH - 1))
        val nAddr = fourBytesToUInt(
            binaryData.sliceArray(
                IntRange(
                    MAGIC_LENGTH + HASH_LENGTH,
                    MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH - 1
                )
            ).toUByteArray()
        )
        val nApps = fourBytesToUInt(
            binaryData.sliceArray(
                IntRange(
                    MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH,
                    HEADER_LENGTH - 1
                )
            )
                .toUByteArray()
        )
        header = Header(magic = magic, hash = hash, nAddr = nAddr, nApps = nApps)
    }

    /**
     * Use this to find appearances for a specific address.
     */
    fun findAppearances(address: String): List<AppearanceRecord> {
        return findAddressRecord(address)?.appearances ?: emptyList()
    }

    fun findAddressRecord(addressToBeFound: String): AddressRecord? {
        val appearanceTableStart = HEADER_LENGTH + (header.nAddr.toInt() * ADDR_RECORD_LENGTH)
        val addressIndex = searchBinaryBlob(
            header.nAddr.toInt()
        ) { i ->
            val addressRecordBytes = binaryData.sliceArray(
                IntRange(
                    HEADER_LENGTH + (i * ADDR_RECORD_LENGTH),
                    HEADER_LENGTH + (i * ADDR_RECORD_LENGTH) + ADDR_RECORD_LENGTH - 1
                )
            )
            assert(addressRecordBytes.size == 28)

            val addressBytes = addressRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1))
            assert(addressBytes.size == 20)
            val addressString = addressBytes.joinToString("", prefix = "0x") { "%02x".format(it) }

            when {
                addressString < addressToBeFound -> -1
                addressString > addressToBeFound -> 1
                else -> 0
            }
        }

        return if (addressIndex == header.nAddr.toInt()) {
            null
        } else {
            val addressRecordBytes = binaryData.sliceArray(
                IntRange(
                    HEADER_LENGTH + (addressIndex * ADDR_RECORD_LENGTH),
                    HEADER_LENGTH + (addressIndex * ADDR_RECORD_LENGTH) + ADDR_RECORD_LENGTH - 1
                )
            )
            assert(addressRecordBytes.size == 28)

            val addressBytes = addressRecordBytes.sliceArray(IntRange(0, ADDR_LENGTH - 1))
            assert(addressBytes.size == 20)
//            val addressString = addressBytes.joinToString("", prefix = "0x") { "%02x".format(it) }

            val appearancesOffset = fourBytesToUInt(
                addressRecordBytes.sliceArray(
                    IntRange(
                        ADDR_LENGTH,
                        ADDR_LENGTH + ADDR_OFFSET_LENGTH - 1
                    )
                )
            )
            val appearancesCount = fourBytesToUInt(
                addressRecordBytes.sliceArray(
                    IntRange(
                        ADDR_LENGTH + ADDR_OFFSET_LENGTH,
                        ADDR_RECORD_LENGTH - 1
                    )
                )
            )

            val appearances = mutableListOf<AppearanceRecord>()
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
                appearances.add(AppearanceRecord(blockNum, txIndex))
            }
            AddressRecord(addressBytes, appearancesOffset, appearancesCount, appearances)
        }
    }

    fun searchBinaryBlob(
        n: Int,
        compareElement: (Int) -> Int
    ): Int {
        var i = 0
        var j = n
        while (i < j) {
            val h = (i + j).ushr(1) // avoid overflow when computing h
            when (compareElement(h)) {
                -1 -> i = h + 1 // Element is smaller, search in the right half
                1 -> j = h // Element is greater, search in the left half
                0 -> return h // Element found
            }
        }
        return n
    }

    /**
     * Parses the binary data to extract address records and their appearances.
     * This method reads the binary data, extracts the address records, and populates the addressRecords map.
     *
     * This is optional and can be called after creating an instance of IndexParser. You can search for appearances without parsing the data.
     *
     */
    fun parseToAddressRecords() {
        val appearanceTableStart = HEADER_LENGTH + (header.nAddr.toInt() * ADDR_RECORD_LENGTH)
        for (addressIndex in 1..<header.nAddr.toInt()) {
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
                addressRecordBytes.sliceArray(
                    IntRange(
                        ADDR_LENGTH,
                        ADDR_LENGTH + ADDR_OFFSET_LENGTH - 1
                    )
                )
            )
            val appearancesCount = fourBytesToUInt(
                addressRecordBytes.sliceArray(
                    IntRange(
                        ADDR_LENGTH + ADDR_OFFSET_LENGTH,
                        ADDR_RECORD_LENGTH - 1
                    )
                )
            )

            val appearances = mutableListOf<AppearanceRecord>()
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
                appearances.add(AppearanceRecord(blockNum, txIndex))
            }
            val addressRecord = AddressRecord(
                addressBytes,
                appearancesOffset,
                appearancesCount,
                appearances = appearances
            )
            addressRecords[addressString] = addressRecord
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

data class IndexFile(val header: Header, val addressRecords: List<AddressRecord>)
data class AddressRecord(
    var address: ByteArray,
    var offset: UInt,
    var count: UInt,
    var appearances: List<AppearanceRecord>
) {
    override fun toString(): String {
        return "AddressRecord(address=${
            address.joinToString(
                "",
                prefix = "0x"
            ) { "%02x".format(it) }
        }, offset=$offset, count=$count), appearances: $appearances"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressRecord

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

data class AppearanceRecord(
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
