import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main(args: Array<String>) {
    println("args: $args")
    val indexParser = IndexParser()
//    indexParser.parse(File("/Users/biafra/trueblocks-kotlin/007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))
//    indexParser.parse(File("/Users/biafra/trueblocks-kotlin/020782939-020785534_QmePxCpxtCQSDVcGTbQaXNARQjs1Us2WH6tXvixQEqZjCG.index"))

//    val bloomParser = BloomParser()
//    bloomParser.parse(File(args[2]))

    val addressChecker = CheckAddress()
//    addressChecker.main("0xfffeb249dddf766425f3ddc81d033b7100cf4c3b", "/Users/biafra/trueblocks-kotlin/007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom")
    addressChecker.main("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79", "/Users/biafra/trueblocks-kotlin/007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom")
//    addressChecker.main("0xffffffffffffffffffffffffee77f8ed97e1ac84", "/Users/biafra/trueblocks-kotlin/020782939-020785534_QmQfn7HkkyjiipBMYvnoQExp7G26NVv17a1pJZyPGpVuf6.bloom")
}

