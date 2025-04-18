import com.jaeckel.trueblocks.IpfsClient
import com.jaeckel.trueblocks.IpfsHttpClient
import com.jaeckel.trueblocks.IpfsLocalClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import org.kethereum.model.Address
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
        .build()
    println(IPFS(IPFSConfiguration(moshi = moshi)).info.version())

//    val addressToCheck = Address("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79")
//    val addressToCheck = "0x7070EA7B152cB66cad751ed7195b115995f08b19".lowercase()  // https://etherscan.io/address/0x7070ea7b152cb66cad751ed7195b115995f08b19
    val addressToCheck =
        args.getOrNull(0)?.lowercase() ?: "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()

    val manifestCID = "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
//    val ipfsClient: IpfsClient = IpfsHttpClient("https://ipfs.unchainedindex.io/ipfs/")
    val ipfsClient: IpfsClient = IpfsLocalClient( "http://127.0.0.1:5001/api/v0/")

    val manifestResponse = ipfsClient.fetchAndParseManifestUrl(manifestCID)
    manifestResponse?.chunks?.reversed()?.forEach {
//    manifestResponse?.chunks?.forEach {
//        println(it)
        val bloom = ipfsClient.fetchBloom(it.bloomHash, it.range)

        bloom?.let { bloom ->
            if (bloom.isMemberBytes(Address(addressToCheck))) {
                // fetch index
                val appearances = ipfsClient.fetchIndex(cid = it.indexHash, parse = false)?.findAppearances(addressToCheck)
                appearances?.forEach { appearance ->
                    println("$addressToCheck \t${appearance.blockNumber} \t${appearance.txIndex}")
                }
            } else {
//                print("Address not found in bloom range: ${bloom.range}\r")
            }
        }
    }

    exitProcess(0)
}
