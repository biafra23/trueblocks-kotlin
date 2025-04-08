import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kethereum.model.Address
import kotlin.system.exitProcess

data class ManifestResponse(
    val version: String,
    val chain: String,
    val specification: String,
    val config: Config,
    val chunks: List<Chunk>
)

data class Config(
    val appsPerChunk: Int,
    val snapToGrid: Int,
    val firstSnap: Int,
    val unripeDist: Int
)

data class Chunk(
    val range: String,
    val bloomHash: String,
    val bloomSize: Int,
    val indexHash: String,
    val indexSize: Int
)

fun fetchAndParseManifestUrl(url: String): ManifestResponse? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return null
        }
        val responseBody = response.body?.string() ?: return null
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
            .build()
        val adapter = moshi.adapter(ManifestResponse::class.java)
        return adapter.fromJson(responseBody)
    }
}

const val ipfsBaseUrl = "https://ipfs.unchainedindex.io/ipfs/"

fun main(args: Array<String>) {

//    val addressToCheck = Address("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79")
    val addressToCheck = "0x7070EA7B152cB66cad751ed7195b115995f08b19".lowercase()  // https://etherscan.io/address/0x7070ea7b152cb66cad751ed7195b115995f08b19


    val url = ipfsBaseUrl + "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    val result = fetchAndParseManifestUrl(url)

    //println("Parsed result: $result")
    println("Number of chunks: ${result?.chunks?.size}")

    result?.chunks?.reversed()?.forEach {
        val bloom = fetchBloom(it.bloomHash, it.range)

        bloom?.let { bloom ->
            if (bloom.isMemberBytes(Address(addressToCheck))) {
                println("\nAddress found in bloom range: ${bloom.range}")
                // fetch index
               val addressRecords = fetchIndex(it.indexHash)?.addressRecords
                // find adress in index
                addressRecords?.let {
                    println("Found ${addressRecords.size} addressRecords.")
//                    println("addressRecords: $addressRecords: ")
                    // print occurrences (if any)
                    val recordFound = addressRecords[addressToCheck]
                    recordFound?.let { record ->
                        println("record found: $record")
                    }
                }
            } else {
                print("Address not found in bloom range: ${bloom.range}\r")
            }
        }
    }

    exitProcess(0)
}

fun fetchBloom(cid: String, range: String = "undefined"): Bloom? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(ipfsBaseUrl + cid)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return null
        }
        response.body?.let { body ->
            //println("body: $body")
            val bytes = body.bytes()
            val size = bytes.size.toLong()
            val bloom = Bloom.Companion.parseBloomBytes(bytes, size)
            bloom.range = range
            return bloom
        }
    }
    return null
}

fun fetchIndex(cid: String): IndexParser? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(ipfsBaseUrl + cid)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return null
        }
        response.body?.let { body ->

            val indexParser = IndexParser()
            val bytes = body.bytes()
            indexParser.parse(bytes)

            return indexParser
        }
    }
    return null
}
