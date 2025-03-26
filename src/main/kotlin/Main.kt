import java.io.File
import java.io.RandomAccessFile

//fun main(args: Array<String>) {
//    println("args: $args")
//    val indexParser = IndexParser()
////    indexParser.parse(File("/Users/biafra/trueblocks-kotlin/007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))
////    indexParser.parse(File("/Users/biafra/trueblocks-kotlin/020782939-020785534_QmePxCpxtCQSDVcGTbQaXNARQjs1Us2WH6tXvixQEqZjCG.index"))
//
//    val bloomFileName = "/Users/biafra/trueblocks-kotlin/007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom"
//    val addressString = "0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"
//    val bloomParser = BloomParser()
//    bloomParser.parse(File(bloomFileName))
//
//    val addressChecker = CheckAddress()
////    addressChecker.main("0xfffeb249dddf766425f3ddc81d033b7100cf4c3b", "/Users/biafra/trueblocks-kotlin/007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom")
//    addressChecker.main(addressString, bloomFileName)
////    addressChecker.main("0xffffffffffffffffffffffffee77f8ed97e1ac84", "/Users/biafra/trueblocks-kotlin/020782939-020785534_QmQfn7HkkyjiipBMYvnoQExp7G26NVv17a1pJZyPGpVuf6.bloom")
//}




fun main() {
    val file = RandomAccessFile("path/to/bloom/file", "r")
    val bloom = Bloom(file, headerSize = 128, count = 10u)
    val address = Address(ByteArray(20) { 0 }) // Replace with actual address bytes

    val isMember = bloom.isMemberBytes(address)
    println("Is member: $isMember")
}

