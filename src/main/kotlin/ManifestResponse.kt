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
