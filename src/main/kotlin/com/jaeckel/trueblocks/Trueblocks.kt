package com.jaeckel.trueblocks

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

    /**
     * override defaults for Manifest CID and IPFS base URL
     */
    fun setup(manisfestCID: String, ipfsBaseUrl: String) {
        this.manifestCID = manisfestCID
        this.ipfsBaseUrl = ipfsBaseUrl
    }

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

    fun queryByAddress(address: String, since: Date? = null, sinceBlockNumber: Int? = null): List<AppearanceRecord> {
        var startBlockNumber = 0
        sinceBlockNumber?.let { blockNumber ->
            startBlockNumber = blockNumber
        }
        since?.let {
            // calculate start block number from date
        }
        val result = mutableListOf<AppearanceRecord>()




        return result
    }
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