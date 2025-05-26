import com.jaeckel.trueblocks.IpfsClient
import com.jaeckel.trueblocks.IpfsHttpClient
import com.jaeckel.trueblocks.IpfsLocalClient
import com.jaeckel.trueblocks.SwarmConnectResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import org.kethereum.model.Address
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

/*
    * This is a simple Kotlin program that demonstrates how to use the IPFS client to fetch and parse Trueblocks data and find relevant transactions for a certain account.
    * It uses the `IpfsClient` (either via http or a locally running node) to fetch a manifest file from IPFS, parse it, and then fetch bloom filters and index chunk files.
 */
fun main(args: Array<String>) {
    val addressToCheck =
        args.getOrNull(0)?.lowercase() ?: "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()

    val manifestCID =
        "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    // Use http gaetway
    val ipfsClient: IpfsClient = IpfsHttpClient("https://ipfs.unchainedindex.io/ipfs/")

    // Use a local IPFS node instead:
//    val moshi = Moshi.Builder()
//        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
//        .build()
//    logger.info("IPFS client version: {}", IPFS(IPFSConfiguration(moshi = moshi)).info.version())
//    val ipfsClient = IpfsLocalClient("http://127.0.0.1:5001/api/v0/")
//    logger.info("Stats: ${ipfsClient.stats().bandWidth()}")  // show bandwidth stats
//    val pinataAddress = "/dnsaddr/bitswap.pinata.cloud"
//    val response = ipfsClient.swarmConnect(pinataAddress)
//    if (response is SwarmConnectResult.Success) {
//        logger.info("Pinata connect result: $response")
//    } else {
//        logger.error("Failed to connect to $pinataAddress: $response")
//        exitProcess(1)
//    }
//    ipfsClient.pinLs() // list pinned CIDs
//    ipfsClient.swarmPeers() // list connected peers

    val manifestResponse = ipfsClient.fetchAndParseManifestUrl(manifestCID)
    // List appearances for the given address. Starting with the latest blocks.
    manifestResponse?.chunks?.reversed()?.forEach {
//    manifestResponse?.chunks?.forEach { // oldest to newest block
        val bloom = ipfsClient.fetchBloom(it.bloomHash, it.range)

        bloom?.let { bloom ->
            if (bloom.isMemberBytes(Address(addressToCheck))) {
                // fetch index
                val appearances = ipfsClient.fetchIndex(cid = it.indexHash, parse = false)
                    ?.findAppearances(addressToCheck)
                appearances?.forEach { appearance ->
                    logger.info("$addressToCheck \t${appearance.blockNumber} \t${appearance.txIndex}")
                }
            } else {
                logger.trace("Address not found in bloom range: ${bloom.range}\r")
            }
        }
    }

    exitProcess(0)
}
