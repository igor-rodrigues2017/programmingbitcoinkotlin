package extension

import java.util.*
import kotlin.math.absoluteValue

fun Stack<ByteArray>.negativeRangeIndex(start: Int, end: Int): List<ByteArray> {
    require(start <= 0 && end <= 0) { "Only negative number" }
    return this.slice(this.size - start.absoluteValue until this.size - end.absoluteValue)
}

fun Stack<ByteArray>.negativeIndex(index: Int): ByteArray {
    require(index < 0 ) { "Only negative number" }
    return this[this.size - index.absoluteValue]
}

fun Stack<ByteArray>.removeNegativeIndex(index: Int): ByteArray {
    require(index < 0 ) { "Only negative number" }
    return this.removeAt(this.size - index.absoluteValue)
}