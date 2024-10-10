import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


private const val N_ADDR_LENGTH = 4
private const val N_APPS_LENGTH = 4
private const val MAGIC_LENGTH = 4
private const val HASH_LENGTH = 32
private const val HEADER_LENGTH = MAGIC_LENGTH + HASH_LENGTH + N_ADDR_LENGTH + N_APPS_LENGTH
private const val ADDR_LENGTH = 20
private const val ADDR_OFFSET_LENGTH = 4
private const val ADDR_COUNT_LENGTH = 4
private const val ADDR_RECORD_LENGTH = ADDR_LENGTH + ADDR_OFFSET_LENGTH + ADDR_COUNT_LENGTH
private const val APP_BLOCKNUM_LENGTH = 4
private const val APP_TX_INDEX_LENGTH = 4
private const val APP_RECORD_LENGTH = APP_BLOCKNUM_LENGTH + APP_TX_INDEX_LENGTH

fun main(args: Array<String>) {

//    val indexFile = File("QmVEpKTCWG8qwxWnbUPefYUJE1Pj58x6141AcQGkcQzpqE.index") // 20mb
   // val indexFile = File("QmevozuTsKZ2pAWw89DGQFbPhzKdDcf141Y1Qhizt5kikr.index") // 330k
//    val indexFile = File("QmaTRnhhocBbSjCaKjsZ63J5MnsHp9PVNR5V9SE6KJ9Ra8.index") // 20mb
//    val indexFile = File("QmSgWqZ6JSEk8ydkqLYv9ncjQjs8HbpmPxfTDEJeWwNgX2.index") // 600kb
//    val indexFile = File("QmetD8FTcPfaXsfoX12q62TNVAFZayVbP8sTojWhkVHxUc.index") // 600kb

    val indexParser = IndexParser()
    args.forEach {
        indexParser.parse(File(it))
    }

//    args.forEach {
//        val bloomParser = BloomParser()
//        bloomParser.parse(File(it))
//    }
}

