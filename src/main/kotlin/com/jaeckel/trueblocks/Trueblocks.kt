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
    fun initializeBloomFilters(progressCallback: (() -> String)? = null) {

        val url = ipfsBaseUrl + manifestCID
        val ipfsHttpClient = IpfsHttpClient()
        val manifestResponse = ipfsHttpClient.fetchAndParseManifestUrl(url)

        manifestResponse?.chunks?.reversed()?.forEach {
            val bloom = ipfsHttpClient.fetchBloom(it.bloomHash, it.range)

            bloom?.let { bloom ->
//                if (bloom.isMemberBytes(Address(addressToCheck))) {
//                    // fetch index
//                    val appearances = ipfsHttpClient.fetchIndex(cid = it.indexHash, parse = false)?.findAppearances(addressToCheck)
//                    appearances?.forEach { appearance ->
//                        println("$addressToCheck \t${appearance.blockNumber} \t${appearance.txIndex}")
//                    }
//                } else {
////                print("Address not found in bloom range: ${bloom.range}\r")
//                }
            }
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

interface ProgressCallback {
    fun onProgressBloomUpdate(blockRangeChecked: IntRange)
    fun onProgressIndexUpdate(blockRangeChecked: IntRange)
    fun onProgressComplete()
    fun onProgressError(error: String)
//    fun on
}