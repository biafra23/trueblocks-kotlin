import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


// Define the cache directory and size (10 MB in this example)
val cacheDirectory = File("cacheDir") // Replace with your desired directory
const val cacheSize = 10L * 1024 * 1024 * 1024 // 1024 * 10 MB
val cache = Cache(cacheDirectory, cacheSize)


class IpfsHttpClient {
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

    fun fetchAndParseManifestUrl(url: String): ManifestResponse? {
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

    fun fetchBloom(cid: String, range: String = "undefined"): Bloom? {
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

    fun fetchIndex(cid: String, parse: Boolean = true): IndexParser? {
        val request = Request.Builder()
            .url(ipfsBaseUrl + cid)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Request ($request) failed: ${response.code} ${response.message}")
                return null
            }
            response.body?.let { body ->

                val bytes = body.bytes()
                val indexParser = IndexParser(bytes)
                if (parse) {
                    indexParser.parseToAddressRecords()
                }

                return indexParser
            }
        }
        return null
    }
}
