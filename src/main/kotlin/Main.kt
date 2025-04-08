import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

data class ManifestResponse(
    val version: String,
    val chain: String,
    val specification: String,
    val config: Config,
    val chunks: List<Chunk>
)

data class Config(
    val appsPerChunk: Int,
    val snapToGrid: Int,
    val firstSnap: Int,
    val unripeDist: Int
)

data class Chunk(
    val range: String,
    val bloomHash: String,
    val bloomSize: Int,
    val indexHash: String,
    val indexSize: Int
)

/*
{
  "version": "trueblocks-core@v0.40.0",
  "chain": "mainnet",
  "specification": "QmUou7zX2g2tY58LP1A2GyP5RF9nbJsoxKTp299ah3svgb",
  "config": {
    "appsPerChunk": 2000000,
    "snapToGrid": 100000,
    "firstSnap": 2300000,
    "unripeDist": 28
  },
  "chunks": [
    {
      "range": "000000000-000000000",
      "bloomHash": "QmYhuaJu9bHAGpSsuaQug7bjnLcoE6B5PCZsg4XZGVFbKy",
      "bloomSize": 131114,
      "indexHash": "QmaKUsfH5AXqPJgjGAQFZWRheF1jtc5PqGi8cYrwmMCXdu",
      "indexSize": 320192
    },
    {
      "range": "000000001-000590510",
      "bloomHash": "QmTex6bV4wmwnHtLhyMK64smwgtnN6oZPMn26863nGkYW7",
      "bloomSize": 131114,
      "indexHash": "QmZ7ByHDQhqe8y8wZoJCk73AGYizG8Nnva7VtAoWGbPdHn",
      "indexSize": 16821332
    }
      ]
}
 */
fun fetchAndParseUrl(url: String): ManifestResponse? {
    // Create an OkHttpClient instance
    val client = OkHttpClient()

    // Build the request
    val request = Request.Builder()
        .url(url)
        .build()

    // Execute the request
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Request failed: ${response.code}")
            return null
        }

        // Get the response body as a string
        val responseBody = response.body?.string() ?: return null

        // Create a Moshi instance
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()) // Add support for Kotlin data classes
            .build()

        // Create a JSON adapter for the data class
        val adapter = moshi.adapter(ManifestResponse::class.java)

        // Parse the JSON response
        return adapter.fromJson(responseBody)
    }
}

fun main() {
    val url = "https://ipfs.unchainedindex.io/ipfs/QmYjUzLhetTPovBydBoVuzCYnDRABWtfjeBL6JbxopadJG"
    val result = fetchAndParseUrl(url)
    println("Parsed result: $result")
}
