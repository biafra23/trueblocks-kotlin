# trueblocks-kotlin

This library aims to read and query the Trueblocks unchained index. It does not cover the generation
of the index.

# Add trueblocks-kotlin to your Gradle project

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

## Usage
Run the tests via command line:
```bash
./gradlew test
```

Run Main.kt via IntelliJ or via command line:
```bash
./gradlew run
```
Pass an address to check via:
```bash
./gradlew run --args="0xfffc3ead0df70e9bbe805af463814c2e6de5ae79"
```

Use either `IpfsHttpClient` or `IpfsLocalClient` to connect to the Trueblocks index. The IpfsHttpClient connects to a remote IPFS node, 
while the IpfsLocalClient connects to a local IPFS node. The latter is recommended for decentralization reasons.

See `Main.kt` for example usage

For an example to use this on Android please have a look at: https://github.com/biafra23/AndroidPortal 