import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kethereum.model.Address
import java.io.File
import kotlin.system.exitProcess

// Define the cache directory and size (10 MB in this example)
val cacheDirectory = File("cacheDir") // Replace with your desired directory
const val cacheSize = 10L * 1024 * 1024 * 1024 // 1024 * 10 MB
val cache = Cache(cacheDirectory, cacheSize)

// Interceptor to force cache usage
val forceCacheInterceptor = Interceptor { chain ->
    var request = chain.request().newBuilder()
        .header("Cache-Control", "only-if-cached, max-stale=${Int.MAX_VALUE}")
        .build()

    val response = chain.proceed(request)

    // If cache is unavailable, fallback to a network request
    if (response.code == 504) {
        request = chain.request().newBuilder()
            .header("Cache-Control", "no-cache")
            .build()
        chain.proceed(request)
    } else {
        response
    }
}
// Build the OkHttpClient with caching and the interceptor
val client = OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor(forceCacheInterceptor)
    .build()

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
//    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code} ${response.message}")
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
//    val addressToCheck = "0x7070EA7B152cB66cad751ed7195b115995f08b19".lowercase()  // https://etherscan.io/address/0x7070ea7b152cb66cad751ed7195b115995f08b19
    val addressToCheck = "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()  // https://etherscan.io/address/0x308686553a1EAC2fE721Ac8B814De638975a276e


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
//    val client = OkHttpClient()
    val request = Request.Builder()
        .url(ipfsBaseUrl + cid)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code} ${response.message}")
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
//    val client = OkHttpClient()
    val request = Request.Builder()
        .url(ipfsBaseUrl + cid)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code} ${response.message}")
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


/*

record found: AddrRecord(address=0x0x308686553a1eac2fe721ac8b814de638975a276e, offset=387800, count=1), appearances: [AppRecord(blockNumber=19997798, txIndex=148)]


{
  "blockHash": "0x025f6f826dbefcc740bca14f98c0f3d83b93d4bb0d1e0f5e7fc27d164e1a1825",
  "blockNumber": "0x1312c26",
  "from": "0xc00d69e9abc8ac20f27467abd2a63d6b169da2cf",
  "gas": "0x54431",
  "gasPrice": "0x14258a9f3",
  "maxFeePerGas": "0x1625343ab",
  "maxPriorityFeePerGas": "0xf4240",
  "hash": "0x5e6d9ac2e4b24cbc74f743ff1d9f08aa7d30984d90863ca84c2d8d06651bb271",
  "input": "0x12aa3caf000000000000000000000000e37e799d5077682fa0a244d46e5649f71457bd09000000000000000000000000be77212a6c7f55567470c2c95aff7b0b0e0c3ef5000000000000000000000000f1df7305e4bab3885cab5b1e4dfc338452a67891000000000000000000000000e37e799d5077682fa0a244d46e5649f71457bd09000000000000000000000000c00d69e9abc8ac20f27467abd2a63d6b169da2cf000000000000000000000000000000000000000000001c6cdf5ec6f200522710000000000000000000000000000000000000000000000000000000334017fc37000000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000001400000000000000000000000000000000000000000000000000000000000000160000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001a000000000000000000000000000000000000000000000018200006800004e802026678dcdbe77212a6c7f55567470c2c95aff7b0b0e0c3ef5382ffce2287252f930e1c8dc9328dac5bf282ba1000000000000000000000000000000000000000000000048c4cb3011c7aee6ca0020d6bdbf78be77212a6c7f55567470c2c95aff7b0b0e0c3ef500a007e5c0d20000000000000000000000000000000000000000000000000000f600008f0c20be77212a6c7f55567470c2c95aff7b0b0e0c3ef5018c55910ae4178517b7eb31da8a4f69de784ee46ae40711b8002dc6c0018c55910ae4178517b7eb31da8a4f69de784ee4051ef36e55875c08e4efaea6072c6d0f66fb0b9f0000000000000000000000000000000000000000000000000090f3d210e46c06be77212a6c7f55567470c2c95aff7b0b0e0c3ef500206ae40711b8002dc6c0051ef36e55875c08e4efaea6072c6d0f66fb0b9f1111111254eeb25477b68fb85ed929f73a960582000000000000000000000000000000000000000000000000000000334017fc37c02aaa39b223fe8d0a0e5c4f27ead9083c756cc29a635db5",
  "nonce": "0x7f",
  "to": "0x1111111254eeb25477b68fb85ed929f73a960582",
  "transactionIndex": "0x94",
  "value": "0x0",
  "type": "0x2",
  "accessList": [],
  "chainId": "0x1",
  "v": "0x0",
  "r": "0xce0b9b58e15e9f62910568239bc6b1e3887465a05762c94ebe3e30a0bbbe3fca",
  "s": "0x79d86861cdb60b896b08d768aa43e24c56cd0ae312ffa26a7b3da5e54e32ba38",
  "yParity": "0x0"
}


record found: AddrRecord(address=0x0x308686553a1eac2fe721ac8b814de638975a276e, offset=430014, count=1), appearances: [AppRecord(blockNumber=19732544, txIndex=120)]



 */
