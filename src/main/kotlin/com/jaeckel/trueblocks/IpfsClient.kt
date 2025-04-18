package com.jaeckel.trueblocks

interface IpfsClient {
    fun fetchAndParseManifestUrl(manifestCID: String): ManifestResponse?
    fun fetchBloom(cid: String, range: String = "undefined"): Bloom?
    fun fetchIndex(cid: String, parse: Boolean = true): IndexParser?
}