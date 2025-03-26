import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

//fun Bloom.writeBloom(fileName: String): Boolean {
//    val file = RandomAccessFile(fileName, "rw")
//    this.file = file
//
//    file.seek(0)
//    val header = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(this.headerSize.toInt()).array()
//    file.write(header)
//
//    file.writeInt(this.count)
//    for (bloom in this.blooms) {
//        file.writeInt(bloom.nInserted)
//        file.write(bloom.bytes)
//    }
//
//    file.close()
//    return true
//}
//
//fun Bloom.updateTag(tag: String, fileName: String): Boolean {
//    val file = RandomAccessFile(fileName, "rw")
//    this.file = file
//
//    file.seek(0)
//    val header = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(this.headerSize.toInt()).array()
//    file.write(header)
//
//    file.close()
//    return true
//}
