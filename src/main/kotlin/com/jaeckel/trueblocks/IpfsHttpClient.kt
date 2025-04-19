package com.jaeckel.trueblocks

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// Define the cache directory and size (10 MB in this example)
val cacheDirectory = File("cacheDir") // Replace with your desired directory
const val cacheSize = 10L * 1024 * 1024 * 1024 // 1024 * 10 MB
val cache = Cache(cacheDirectory, cacheSize)

class IpfsHttpClient(val ipfsBaseUrl: String = "https://ipfs.unchainedindex.io/ipfs/") :
    IpfsClient {
    // Interceptor to force cache usage
    private val forceCacheInterceptor = Interceptor { chain ->
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

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set the desired logging level
    }

    // Build the OkHttpClient with caching and the interceptor
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(RedirectLoggingInterceptor())
        .addInterceptor(loggingInterceptor)
        .cache(cache)
        .addInterceptor(forceCacheInterceptor)
        .build()


    override fun fetchAndParseManifestUrl(manifestCID: String): ManifestResponse? {
        try {
            val request = Request.Builder()
                .url(ipfsBaseUrl + manifestCID)
                .build()
            println("request: $request")
            okHttpClient.newCall(request).execute().use { response ->
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
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }
    }

    override fun fetchBloom(cid: String, range: String): Bloom? {
        try {
            val request = Request.Builder()
                .url(ipfsBaseUrl + cid)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Request failed: ${response.code} ${response.message}")
                    return null
                }
                response.body?.let { body ->
                    //println("body: $body")
                    val bytes = body.bytes()
                    val size = bytes.size.toLong()
                    val bloom = Bloom.parseBloomBytes(bytes, size)
                    bloom.range = range
                    return bloom
                }
            }
            return null
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }

    }

    override fun fetchIndex(cid: String, parse: Boolean): IndexParser? {
        try {
            val request = Request.Builder()
                .url(ipfsBaseUrl + cid)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
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
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }
    }
}

class RedirectLoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isRedirect) {
            val redirectUrl = response.header("Location")
            println("Redirect from ${request.url} to $redirectUrl")
        }

        return response
    }
}