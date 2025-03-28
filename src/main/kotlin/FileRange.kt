//package base
//
//import java.nio.file.Paths
//import java.util.regex.Pattern
//
//data class RecordRange(
//    val first: ULong,
//    val last: ULong
//)
//
//data class FileRange(
//    val first: Blknum,
//    val last: Blknum
//)
//
//typealias BlockRange = FileRange
//
//data class TimestampRange(
//    val first: Timestamp,
//    val last: Timestamp
//)
//
//val NotARange = FileRange(NOPOSN, NOPOSN)
//
//fun rangeFromFilename(path: String): FileRange {
//    return rangeFromFilenameE(path).first
//}
//
//fun rangeFromFilenameE(path: String): Pair<FileRange, Exception?> {
//    val fileName = Paths.get(path).fileName.toString()
//    val fn = if (fileName.contains(".")) fileName.split(".")[0] else fileName
//    val digitCheck = Pattern.compile("^[0-9]+$")
//
//    if (!digitCheck.matcher(fn.replace("-", "")).matches()) {
//        return Pair(NotARange, IllegalArgumentException("not a valid range $fn"))
//    }
//
//    val parts = fn.split("-")
//    return if (parts.size > 1) {
//        val first = parts[0].trimStart('0').toULong()
//        val last = parts[1].trimStart('0').toULong()
//        Pair(FileRange(first, last), null)
//    } else {
//        val first = parts[0].trimStart('0').toULong()
//        Pair(FileRange(first, first), null)
//    }
//}
//
//fun rangeFromRangeString(rngStr: String): FileRange {
//    return rangeFromFilename(Paths.get(config.pathToIndex("mainnet"), "finalized", "$rngStr.bin").toString())
//}
//
//fun FileRange.toFilename(chain: String): String {
//    return Paths.get(config.pathToIndex(chain), "finalized", this.toString() + ".bin").toString()
//}
//
//fun FileRange.follows(needle: FileRange, sequential: Boolean): Boolean {
//    return if (sequential) {
//        this.first == needle.last + 1u
//    } else {
//        this.laterThan(needle)
//    }
//}
//
//fun FileRange.precedes(needle: FileRange, sequential: Boolean): Boolean {
//    return if (sequential) {
//        if (needle.first == 0uL) {
//            false
//        } else {
//            this.last == needle.first - 1u
//        }
//    } else {
//        this.earlierThan(needle)
//    }
//}
//
//fun FileRange.intersects(needle: FileRange): Boolean {
//    return !this.earlierThan(needle) && !this.laterThan(needle)
//}
//
//fun FileRange.earlierThan(needle: FileRange): Boolean {
//    return this.last < needle.first
//}
//
//fun FileRange.laterThan(needle: FileRange): Boolean {
//    return this.first > needle.last
//}
//
//fun FileRange.intersectsB(bn: Blknum): Boolean {
//    return this.intersects(FileRange(bn, bn))
//}
//
//fun FileRange.earlierThanB(bn: Blknum): Boolean {
//    return this.earlierThan(FileRange(bn, bn))
//}
//
//fun FileRange.laterThanB(bn: Blknum): Boolean {
//    return this.laterThan(FileRange(bn, bn))
//}
//
//fun FileRange.equals(needle: FileRange): Boolean {
//    return this.first == needle.first && this.last == needle.last
//}
//
//fun FileRange.span(): Blknum {
//    return this.last - this.first + 1u
//}
//
//data class RangeDiff(
//    val min: Blknum,
//    val inRange: Blknum,
//    val mid: Blknum,
//    val out: Blknum,
//    val max: Blknum
//)
//
//fun FileRange.overlaps(test: FileRange): RangeDiff {
//    val min = minOf(this.first, test.first)
//    val inRange = maxOf(this.first, test.first)
//    val out = minOf(this.last, test.last)
//    val max = maxOf(this.last, test.last)
//    val mid = (max - min) / 2u + min
//    return RangeDiff(min, inRange, mid, out, max)
//}
