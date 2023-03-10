package extension

import org.bouncycastle.util.BigIntegers
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.math.BigInteger.ZERO

const val BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
const val VARINT_FD = 0xfd.toByte()
const val VARINT_FE = 0xfe.toByte()
const val VARINT_FF = 0xff.toByte()
fun ByteArray.toHex() = this.joinToString("") { String.format("%02x", it) }

fun ByteArray.encodeBase58(): String {
    BigInteger(1, this).let {
        val replaceZerosTo1 = "1".repeat(countZerosBytes())
        return replaceZerosTo1 + encode(it)
    }
}

private fun encode(number: BigInteger): String {
    var number1 = number
    var result = ""
    while (number1 > ZERO) {
        val (newNumber, mod) = number1.divideAndRemainder(58.toBigInteger())
        result = BASE58_ALPHABET[mod.toInt()] + result
        number1 = newNumber
    }
    return result
}

private fun ByteArray.countZerosBytes(): Int {
    var count = 0
    for (c in this) {
        if (c == 0.toByte()) count++ else break
    }
    return count
}

fun ByteArray.encodeBase58CheckSum() = (this + hash256(this).copyOfRange(0, 4)).encodeBase58()

fun ByteArray.littleEndianToBigInteger(): BigInteger = BigIntegers.fromUnsignedByteArray(this.reversedArray())

fun ByteArrayInputStream.readVarint(): BigInteger {
    return when (val bytePrefix = this.readNBytes(1).first()) {
        VARINT_FD -> this.readNBytes(2).littleEndianToBigInteger()
        VARINT_FE -> this.readNBytes(4).littleEndianToBigInteger()
        VARINT_FF -> this.readNBytes(8).littleEndianToBigInteger()
        else -> bytePrefix.toLong().toBigInteger()
    }
}