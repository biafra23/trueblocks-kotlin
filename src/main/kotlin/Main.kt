import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kethereum.model.Address
import java.io.File
import kotlin.system.exitProcess

const val ipfsBaseUrl = "https://ipfs.unchainedindex.io/ipfs/"

fun main(args: Array<String>) {

//    val addressToCheck = Address("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79")
//    val addressToCheck = "0x7070EA7B152cB66cad751ed7195b115995f08b19".lowercase()  // https://etherscan.io/address/0x7070ea7b152cb66cad751ed7195b115995f08b19
    val addressToCheck =
        "0x308686553a1eac2fe721ac8b814de638975a276e".lowercase()  // https://etherscan.io/address/0x308686553a1EAC2fE721Ac8B814De638975a276e

    val url = ipfsBaseUrl + "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    val ipfsHttpClient = IpfsHttpClient()
    val result = ipfsHttpClient.fetchAndParseManifestUrl(url)

    //println("Parsed result: $result")
//    println("Number of chunks: ${result?.chunks?.size}")

//    result?.chunks?.reversed()?.forEach {
    result?.chunks?.forEach {
        val bloom = ipfsHttpClient.fetchBloom(it.bloomHash, it.range)

        bloom?.let { bloom ->
            if (bloom.isMemberBytes(Address(addressToCheck))) {
//                println("\nAddress found in bloom range: ${bloom.range}")
                // fetch index
                val appearances = ipfsHttpClient.fetchIndex(cid = it.indexHash, parse = false)?.findAppearances(addressToCheck)
                appearances?.forEach { appearance ->
                    println("$addressToCheck \t${appearance.blockNumber} \t${appearance.txIndex}")
                }
//                exitProcess(0)

            } else {
//                print("Address not found in bloom range: ${bloom.range}\r")
            }
        }
    }

    exitProcess(0)
}
