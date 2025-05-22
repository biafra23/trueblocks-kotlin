import com.jaeckel.trueblocks.IpfsClient
import com.jaeckel.trueblocks.IpfsHttpClient
import com.jaeckel.trueblocks.IpfsLocalClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.IPFSConfiguration
import org.kethereum.model.Address
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

/*
    * This is a simple Kotlin program that demonstrates how to use the IPFS client to fetch and parse a Trueblocks data and find relevant transactions for a certain account.
    * It uses the IPFS client (either via http or a locally running node) to fetch a manifest file from IPFS, parse it, and then fetch bloom filters and index chunk files.
 */
fun main(args: Array<String>) {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
        .build()
//    println(IPFS(IPFSConfiguration(moshi = moshi)).info.version())
    logger.info("IPFS client version: {}", IPFS(IPFSConfiguration(moshi = moshi)).info.version())

//    val addressToCheck = Address("0xfffc3ead0df70e9bbe805af463814c2e6de5ae79")
//    val addressToCheck = "0x7070EA7B152cB66cad751ed7195b115995f08b19".lowercase()  // https://etherscan.io/address/0x7070ea7b152cb66cad751ed7195b115995f08b19
    val addressToCheck =
        args.getOrNull(0)?.lowercase() ?: "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()

    val manifestCID =
        "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    val ipfsClient: IpfsClient = IpfsHttpClient("https://ipfs.unchainedindex.io/ipfs/")

    // Use a local IPFS node instead:
//    val ipfsClient = IpfsLocalClient("http://127.0.0.1:5001/api/v0/")
//    logger.info("Stats: ${ipfsClient.stats().bandWidth()}")  // show bandwidth stats
//    val pinataAddress = "/dnsaddr/bitswap.pinata.cloud"
//    val result = ipfsClient.swarmConnect(pinataAddress)
//    logger.info("Connecting to $pinataAddress: [$result]")  // connect to pinata
//    ipfsClient.swarmConnect("/ip4/137.184.243.187/tcp/3000/ws/p2p/Qma8ddFEQWEU8ijWvdxXm3nxU7oHsRtCykAaVz8WUYhiKn")
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
