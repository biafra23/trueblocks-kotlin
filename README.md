# trueblocks-kotlin

This library aims to read and query the [Trueblocks](https://trueblocks.io) unchained index. It does not cover the generation
of the index.

## Usage: Command line

Run Main.kt
```bash
./gradlew run
```
Pass an address to check via:
```bash
./gradlew run --args="0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"
```
Run the tests:
```bash
./gradlew test
```

## Usage as a library

### Add trueblocks-kotlin to your Gradle project

In `settings.gradle.kts`:
```kotlin 
repositories {
    maven("https://jitpack.io")
}
```
In `build.gradle.kts`:
```kotlin 
dependencies {
    implementation("com.github.biafra23:trueblocks-kotlin:main-SNAPSHOT")
}
```

Use either `IpfsHttpClient` or `IpfsLocalClient` to connect to the Trueblocks index. The `IpfsHttpClient` connects to a remote IPFS node via https, 
while the `IpfsLocalClient` connects to a local IPFS node. The latter is recommended for decentralization reasons.

### Imports

```kotlin
import com.jaeckel.trueblocks.IpfsClient
import com.jaeckel.trueblocks.IpfsHttpClient     // only for HTTP
import com.jaeckel.trueblocks.IpfsLocalClient    // only for IPFS
import com.jaeckel.trueblocks.SwarmConnectResult
import org.kethereum.model.Address
import kotlin.system.exitProcess
```

### Fetch Trueblocks Manifest

<details open>
<summary>Use via HTTP Gateway</summary>

```kotlin
   val manifestCID =
        "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    val ipfsClient: IpfsClient = IpfsHttpClient("https://ipfs.unchainedindex.io/ipfs/")
    val manifestResponse = ipfsClient.fetchAndParseManifestUrl(manifestCID)
```
</details>
<details>
<summary>Use via IPFS node</summary>

```kotlin
   val manifestCID =
        "QmUBS83qjRmXmSgEvZADVv2ch47137jkgNbqfVVxQep5Y1" // version trueblocks-core@v2.0.0-release
    val ipfsClient = IpfsLocalClient("http://127.0.0.1:5001/api/v0/")
    // Add Pinata node for reliable IPFS access to trueblocks data
    val pinataAddress = "/dnsaddr/bitswap.pinata.cloud"
    val response = ipfsClient.swarmConnect(pinataAddress)
    if (response is SwarmConnectResult.Success) {
        logger.info("Pinata connect result: $response")
    } else {
        logger.error("Failed to connect to $pinataAddress: $response")
        exitProcess(1)
    }
    // Fetch manifest
    val manifestResponse = ipfsClient.fetchAndParseManifestUrl(manifestCID)
```
</details>

The `chunks` section of the manifest should have entries like this: 
``` JSON
{
      "bloomHash": "QmdREgbfg2kWNYLps12G8sR1tLxDBM67nNoNevcbLVWyK5",
      "bloomSize": 131114,
      "indexHash": "QmevozuTsKZ2pAWw89DGQFbPhzKdDcf141Y1Qhizt5kikr",
      "indexSize": 320192,
      "range": "000000000-000000000"
}
```


### Fetch Bloom filter

```kotlin
    val bloom = ipfsClient.fetchBloom(bloomHash, range)
```
### Match an address against the bloom filter

```kotlin
    val addressToCheck = "0x308686553a1EAC2fE721Ac8B814De638975a276e".lowercase()
    isMember = bloom.isMemberBytes(Address(addressToCheck))
```

### Fetch Trueblocks index chunk files and find appearances
```Kotlin
    val appearances = ipfsClient.fetchIndex(cid = it.indexHash, parse = false)?.findAppearances(addressToCheck)
```

See `Main.kt` for a full example

For an example to use this on Android please have a look at: https://github.com/biafra23/AndroidPortal 