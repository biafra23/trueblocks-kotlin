package com.jaeckel.trueblocks

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import io.ipfs.kotlin.commands.Add
import io.ipfs.kotlin.commands.PinLsListResult
import io.ipfs.kotlin.commands.Stats
import io.ipfs.kotlin.commands.StringsResult
import io.ipfs.kotlin.commands.SwarmPeersResult
import io.ipfs.kotlin.model.Strings
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/*
    This is an IPFS client that connects to a local IPFS node.
    It uses the IPFS Kotlin library to interact with the IPFS node.
    The client is configured to connect to a local IPFS node running on port 5001.
    The client can fetch and parse manifests, bloom filters, and index files from IPFS.
    It also provides methods to connect to swarm addresses, list connected peers, and get bandwidth stats.
 */
class IpfsLocalClient(baseUrl: String = "http://127.0.0.1:5001/api/v0/") : IpfsClient {
    private val logger = LoggerFactory.getLogger(IpfsLocalClient::class.java)

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set the desired logging level
    }
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(RedirectLoggingInterceptor())
//        .addInterceptor(loggingInterceptor)
        .build()
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
        .build()
    val ipfs = IPFS(IPFSConfiguration(baseUrl, okHttpClient, moshi))

    init {
        logger.trace("${ipfs.info.version()}")
    }
/*
    Fetch and parse a manifest from IPFS.
    The given manifestCID is used to display for showing progress.

    Note: This is a blocking call and may take some time to complete.
    It is recommended to run this in a background thread or coroutine.
    Example:
        ipfsClient.fetchAndParseManifestUrl("QmXk5v2x6Z1z7g3f8c4e5d6f7g8h9i0j1k2l3m4n5o6p7q")
 */
    override fun fetchAndParseManifestUrl(manifestCID: String): ManifestResponse? {
        try {
            val success = ipfs.pins.add(manifestCID)
            if (!success) {
                logger.error("Failed to pin manifest CID: $manifestCID")
            }
            val manifest = ipfs.get.cat(manifestCID)
            val adapter = moshi.adapter(ManifestResponse::class.java)
            return adapter.fromJson(manifest)
        } catch (e: Exception) {
            logger.error("Exception: ${e.message}")
            return null
        }
    }
/*
    Fetch a bloom filter from IPFS.
    The given range is used to display for showing progress. You can get the CIDs for bloom filter files from the manifest.

    Note: This is a blocking call and may take some time to complete.
    It is recommended to run this in a background thread or coroutine.
    Example:
        ipfsClient.fetchBloom("QmXk5v2x6Z1z7g3f8c4e5d6f7g8h9i0j1k2l3m4n5o6p7q", "0-100")
 */
    override fun fetchBloom(cid: String, range: String): Bloom? {
//        logger.info("fetchBloom: $cid")
        try {
            val success = ipfs.pins.add(cid)
            if (!success) {
                logger.info("Failed to pin manifest CID: $cid")
            }
            val bloomBytes = ipfs.get.catBytes(cid)
            val bloom = Bloom.parseBloomBytes(bloomBytes, bloomBytes.size.toLong())
            bloom.range = range
            return bloom
        } catch (e: Exception) {
            logger.error("Exception: ${e.message}")
            return null
        }
    }

    override fun fetchIndex(cid: String, parse: Boolean): IndexParser? {
//        logger.debug("fetchIndex: $cid")
        try {
            val success = ipfs.pins.add(cid)
            if (!success) {
                logger.error("Failed to pin manifest CID: $cid")
            }
            val indexBytes = ipfs.get.catBytes(cid)
            val indexParser = IndexParser(indexBytes)
            if (parse) {
                indexParser.parseToAddressRecords()
            }
            return indexParser
        } catch (e: Exception) {
            logger.error("Exception: ${e.message}")
            return null
        }

    }

    fun lastError() {
        val lastError = ipfs.lastError
        if (lastError != null) {
            logger.info("Last error: ${lastError.Message}")
        } else {
            logger.debug("No errors")
        }
    }
/*
    Get the bandwidth stats.
    Note: This is a blocking call and may take some time to complete.
    It is recommended to run this in a background thread or coroutine.
    Example:
        ipfs.stats()
        ipfs.stats().bandWidth()
        ipfs.stats().bandWidth().totalIn
        ipfs.stats().bandWidth().totalOut
 */
    fun stats() : Stats {
        return ipfs.stats
    }

    /*
        Connect to a swarm address.
        Note: This is a blocking call and may take some time to complete.
        It is recommended to run this in a background thread or coroutine.
        Example address: "/dnsaddr/bitswap.pinata.cloud"
        Pinata has all the Trueblocks files pinned.
     */
    fun swarmConnect(address: String): SwarmConnectResult {
        when (val result = ipfs.swarm.connect(address)) {
            is StringsResult.Success -> {
                logger.trace("Connected to $address: [${result.result.Strings[0]}]")
                return SwarmConnectResult.Success(result.result)
            }
            is StringsResult.Failure -> {
                logger.error("Failed to connect to $address: [${result.errorMessage}]")
                return SwarmConnectResult.Failure(result.errorMessage)
            }
        }
    }
/*
    List connected peers.
    Note: This is a blocking call and may take some time to complete.
    It is recommended to run this in a background thread or coroutine.
 */
    fun swarmPeers() {
        when (val swarmPeersResult = ipfs.swarm.peers()) {
            is SwarmPeersResult.Success -> {
                val peers = swarmPeersResult.peers.peers
                peers?.forEach {
                    logger.info("Peer: ${it.peer} - Address: ${it.addr}")
                }
//                logger.info("--> Connected peers: ${peers}")
            }

            is SwarmPeersResult.Failure -> {
                logger.info("--> Failed to get swarm peers: ${swarmPeersResult.errorMessage}")
            }
        }
    }
/*
    List all pinned CIDs.
    Note: This is a blocking call and may take some time to complete.
    It is recommended to run this in a background thread or coroutine.
 */
    fun pinLs() {
        when (val pinList = ipfs.pins.ls()) {
            is PinLsListResult.Success -> {
                pinList.pinLsList.keys?.forEach { (key, value) ->
                    logger.info("Key: $key, Name: ${value.name}, Type: ${value.type}")
                }
            }
            is PinLsListResult.Failure -> {
                logger.error("Failed to get pin ls: ${pinList.errorMessage}")
            }
        }
    }
}

sealed class SwarmConnectResult {
    data class Success(val result: Strings) : SwarmConnectResult()
    data class Failure(val errorMessage: String) : SwarmConnectResult()
}