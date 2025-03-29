import org.kethereum.model.Address
import java.io.File

fun main(args: Array<String>) {
    println("args: $args")
    val indexParser = IndexParser()
    indexParser.parse(File("/Users/biafra/trueblocks-kotlin//007799261-007800000_QmQBf3PAoFfUaJZcsCQDj4iSziN56xmyaDiQGM12bTkWdE.index"))
//   indexParser.parse(File("/Users/biafra/trueblocks-kotlin/020782939-020785534_QmePxCpxtCQSDVcGTbQaXNARQjs1Us2WH6tXvixQEqZjCG.index"))

    val bloomFileName = "./007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom"
//    val bloomFileName = "/Users/biafra/trueblocks-kotlin/QmR5XPnYuJuypCu8LzbWKki3i1DnfWZsgJF9JsqPWL2hCF.bloom"
//    val bloomFileName = "/Users/biafra/trueblocks-kotlin/020782939-020785534_QmQfn7HkkyjiipBMYvnoQExp7G26NVv17a1pJZyPGpVuf6.bloom"
    val addressString = "0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"
//    val addressString = "0x56EE04A8599B0988C86Cdcdf9A815403A99a5639"
//    val addressString = "0xb6703c0A4c1e856e48fcF3E41C2621e1743a6fEC" //  https://etherscan.io/tx/0xb59a859486bed9baa911d3d61a36c13b2c9a684173047dbb876d99dcb498808e
//    val bloomParser = BloomParser()
//    bloomParser.parse(File(bloomFileName))

    val bloom = Bloom.openBloom(File(bloomFileName), false)
    val stats = bloom.getStats()
//    println("stats: $stats")

   val isMember = bloom.isMemberBytes(Address(addressString))

    println("isMember: $isMember")
    //val addressChecker = CheckAddress()
//    addressChecker.main("0xfffeb249dddf766425f3ddc81d033b7100cf4c3b", "/Users/biafra/trueblocks-kotlin/007799261-007800000_QmSCAPfekmwUG2UrC8tQzjfh3mRq9ns7515ookmwYuWaUq.bloom")
    //addressChecker.main(addressString, bloomFileName)
//    addressChecker.main("0xffffffffffffffffffffffffee77f8ed97e1ac84", "/Users/biafra/trueblocks-kotlin/020782939-020785534_QmQfn7HkkyjiipBMYvnoQExp7G26NVv17a1pJZyPGpVuf6.bloom")
}



//
//fun main() {
//    val file = RandomAccessFile("QmdDR7VXHVyomhCGATpGEL6wtFy13CbigwxsVugW8xQUfk.bloom", "r")
//    val bloom = Bloom(file, headerSize = 128, count = 10u)
//    val address = Address(ByteArray(20) { 0 }) // Replace with actual address bytes
//
//    val isMember = bloom.isMemberBytes(address)
//    println("Is member: $isMember")
//}
//
