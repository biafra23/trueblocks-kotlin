package com.jaeckel.trueblocks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.Callback
import org.kethereum.model.Address
import java.util.Date

class Trueblocks {

    private var ipfsBaseUrl: String
    private var manifestCID: String

    init {
        // Default values
        this.manifestCID = "QmXk5v2x6Z1z7g3f8c4e5d6f7g8h9i0j1k2l3m4n5o6p7q"
        this.ipfsBaseUrl = "https://ipfs.io/ipfs/"
    }

//    /**
//     * override defaults for Manifest CID and IPFS base URL
//     */
//    fun setup(manisfestCID: String, ipfsBaseUrl: String) {
//        this.manifestCID = manisfestCID
//        this.ipfsBaseUrl = ipfsBaseUrl
//    }

    /**
     * Initialize the Trueblocks library by downloading the manifest and all bloom filters ca. 6.6GB of data.
     */
    fun initializeBloomFilters(progressCallback: BloomProgressCallback? = null) {

        val url = ipfsBaseUrl + manifestCID
        val ipfsHttpClient = IpfsHttpClient()
        val manifestResponse = ipfsHttpClient.fetchAndParseManifestUrl(url)

        val numberOfChunks = manifestResponse?.chunks?.size ?: 0
        var chunksDownloaded = 0
        manifestResponse?.chunks?.reversed()?.forEach { chunk ->

            ipfsHttpClient.fetchBloom(chunk.bloomHash, chunk.range)
            chunksDownloaded++
            progressCallback?.onCompletion(chunksDownloaded, numberOfChunks)
            progressCallback?.onProgressBloomUpdate(chunk.range.toIntRange() ?: IntRange(0, 0))

        }
    }

//    fun queryByAddress(address: String, since: Date? = null, sinceBlockNumber: Int? = null): List<AppearanceRecord> {
//        var startBlockNumber = 0
//        sinceBlockNumber?.let { blockNumber ->
//            startBlockNumber = blockNumber
//        }
//        since?.let {
//            // calculate start block number from date
//        }
//        val result = mutableListOf<AppearanceRecord>()
//
//
//
//
//        return result
//    }


    // State management
    private enum class State {
        UNINITIALIZED,
        INITIALIZING,
        READY,
        ERROR
    }
    private var state: State = State.UNINITIALIZED

    // Coroutines-based initialization
    suspend fun initialize(
        manifestCID: String? = null,
        ipfsBaseUrl: String? = null,
        progressCallback: BloomProgressCallback? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (state == State.INITIALIZING) {
            return@withContext Result.failure(IllegalStateException("Already initializing"))
        }

        try {
            state = State.INITIALIZING
            manifestCID?.let { this@Trueblocks.manifestCID = it }
            ipfsBaseUrl?.let { this@Trueblocks.ipfsBaseUrl = it }

            initializeBloomFilters(progressCallback)
            state = State.READY
            Result.success(Unit)
        } catch (e: Exception) {
            state = State.ERROR
            Result.failure(e)
        }
    }

    // Query interface that works with partial data
    suspend fun queryByAddress(
        address: String,
        since: Date? = null,
        sinceBlockNumber: Int? = null,
        progressCallback: IndexProgressCallback? = null
    ): Flow<AppearanceRecord> = flow {
        // Check if we can serve the query
        if (state == State.UNINITIALIZED) {
            throw IllegalStateException("Trueblocks not initialized. Call initialize() first")
        }

        // Even if still initializing, we might be able to serve partial results
        // based on already loaded bloom filters

        // Implementation here...
    }

//    // Status checking
//    fun getInitializationStatus(): State = state
//
//    fun getLoadedBlockRange(): IntRange? {
//        // Return the range of blocks for which we have bloom filters loaded
//        return when (state) {
//            State.READY -> // return full range
//                State.INITIALIZING -> // return partial range
//            else -> null
//        }
//    }




}

interface BloomProgressCallback {
    fun onManifestFetched(manifestCID: String, manifestResponse: ManifestResponse)
    fun onProgressBloomUpdate(blockRangeFetched: IntRange)
    fun onCompletion(x: Int, ofy: Int)
    fun onError(error: String) // retryable
    fun onFailure(message: String)  // final failure
}

interface IndexProgressCallback {
    fun onProgressIndexUpdate(blockRangeFetched: IntRange)
    fun onComplete()
    fun onError(error: String)
//    fun on
}

fun String.toIntRange(): IntRange? {
    val parts = this.split("-")
    if (parts.size != 2) {
        return null // Invalid format
    }
    val start = parts[0].trim().toIntOrNull()
    val end = parts[1].trim().toIntOrNull()
    if (start == null || end == null) {
        return null // Not valid integers
    }
    return start..end
}