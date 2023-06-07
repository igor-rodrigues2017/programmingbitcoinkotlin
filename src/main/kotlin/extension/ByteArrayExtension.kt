package extension

import org.bouncycastle.util.BigIntegers
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.math.BigInteger.*

const val BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
val SIGHASH_ALL = ONE
val SIGHASH_NONE = TWO
val SIGHASH_SINGLE = 3.toBigInteger()
const val VARINT_FD = 0xfd
const val VARINT_FE = 0xfe
const val VARINT_FF = 0xff
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

fun h160ToP2pkhAddress(hash160: ByteArray, testnet: Boolean = false): String {
    val prefix = if (testnet) byteArrayOf(0x6f) else byteArrayOf(0x00)
    return (prefix + hash160).encodeBase58CheckSum()
}

fun h160ToP2shAddress(hash160: ByteArray, testnet: Boolean = false): String {
    val prefix = if (testnet) byteArrayOf(0xc4.toByte()) else byteArrayOf(0x05)
    return (prefix + hash160).encodeBase58CheckSum()
}

fun ByteArray.littleEndianToBigInteger(): BigInteger = BigIntegers.fromUnsignedByteArray(this.reversedArray())

fun ByteArray.toBigInteger(): BigInteger = BigIntegers.fromUnsignedByteArray(this)

fun ByteArrayInputStream.readVarInt(): BigInteger {
    return when (val bytePrefix = this.read()) {
        VARINT_FD -> this.readNBytes(2).littleEndianToBigInteger()
        VARINT_FE -> this.readNBytes(4).littleEndianToBigInteger()
        VARINT_FF -> this.readNBytes(8).littleEndianToBigInteger()
        else -> bytePrefix.toLong().toBigInteger()
    }
}