package com.jaeckel.trueblocks

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import io.ipfs.kotlin.commands.StringsResult
import io.ipfs.kotlin.commands.SwarmPeersResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class IpfsLocalClient(baseUrl: String = "http://127.0.0.1:5001/api/v0/") : IpfsClient {

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set the desired logging level
    }
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(RedirectLoggingInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
        .build()
    val ipfs = IPFS(IPFSConfiguration(baseUrl, okHttpClient, moshi))

    init {
        println(ipfs.info.version())
    }

    override fun fetchAndParseManifestUrl(manifestCID: String): ManifestResponse? {
        try {
            val manifest = ipfs.get.cat(manifestCID)
            val adapter = moshi.adapter(ManifestResponse::class.java)
            return adapter.fromJson(manifest)
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }
    }

    override fun fetchBloom(cid: String, range: String): Bloom? {
//        println("fetchBloom: $cid")
        try {
            val bloomBytes = ipfs.get.catBytes(cid)
            val bloom = Bloom.parseBloomBytes(bloomBytes, bloomBytes.size.toLong())
            bloom.range = range
            return bloom
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }

    }

    override fun fetchIndex(cid: String, parse: Boolean): IndexParser? {
//        println("fetchIndex: $cid")
        try {
            val indexBytes = ipfs.get.catBytes(cid)
            val indexParser = IndexParser(indexBytes)
            if (parse) {
                indexParser.parseToAddressRecords()
            }
            return indexParser
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            return null
        }

    }

    fun lastError() {
        val lastError = ipfs.lastError
        if (lastError != null) {
            println("Last error: ${lastError.Message}")
        } else {
            println("No errors")
        }
    }

    fun stats() {
        val stats = ipfs.stats
        println("Stats: ${stats.bandWidth()}")

    }

    fun swarmConnect(address: String) {
        when (val result = ipfs.swarm.connect(address)) {
            is StringsResult.Success -> println("Connected to $address: ${result.result}")
            is StringsResult.Failure -> println("Failed to connect to $address: ${result.errorMessage}")
        }
    }

    fun swarmPeers() {
        when (val swarmPeersResult = ipfs.swarm.peers()) {
            is SwarmPeersResult.Success -> {
                val peers = swarmPeersResult.peers
                println("--> Connected peers: $peers")
            }

            is SwarmPeersResult.Failure -> {
                println("--> Failed to get swarm peers: ${swarmPeersResult.errorMessage}")
            }
        }
    }
}