
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

//fun Bloom.read(fileName: String): Boolean {
//    val file = RandomAccessFile(fileName, "r")
//    this.file = file
//
//    file.seek(0)
//    if (!readHeader(true)) {
//        return false
//    }
//
//    this.count = file.readUInt()
//    this.blooms = MutableList(this.count.toInt()) {
//        val nInserted = file.readUInt()
//        val bytes = ByteArray(BLOOM_WIDTH_IN_BYTES)
//        file.read(bytes)
//        Bloom.BloomBytes(nInserted, bytes)
//    }
//
//    file.close()
//    return true
//}

//private fun Bloom.readHeader(check: Boolean): Boolean {
//    val file = this.file ?: return false
//    val header = ByteArray(4)
//    file.read(header)
//    this.headerSize = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
//
//    if (check) {
//        // Add any additional header checks here
//    }
//
//    return true
//}
