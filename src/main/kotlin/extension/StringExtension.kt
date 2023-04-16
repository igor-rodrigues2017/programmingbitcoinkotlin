package extension

import java.math.BigInteger.ZERO

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun String.decodeBase58(): ByteArray {
    val combined = decodeIn25Bytes()
    verifyChecksum(getChecksum(combined), hash256(getHashWithNetworkPrefix(combined)))
    return getHash160(combined)
}

private fun String.decodeIn25Bytes(): ByteArray {
    var num = ZERO
    this.forEach {
        num *= 58.toBigInteger()
        num += BASE58_ALPHABET.indexOf(it).toBigInteger()
    }
    return num.to25ByteArray()
}

private fun getChecksum(combined: ByteArray) = combined.copyOfRange(21, 25)

private fun getHashWithNetworkPrefix(combined: ByteArray) = combined.copyOfRange(0, 21)

private fun getHash160(combined: ByteArray) = combined.copyOfRange(1, 21)

private fun verifyChecksum(checksum: ByteArray, hash: ByteArray) {
    if (checksumIsDifferent(checksum, hash)) {
        throw IllegalArgumentException("Bad address")
    }
}

private fun checksumIsDifferent(checksum: ByteArray, hash: ByteArray) = !checksum.contentEquals(hash.copyOfRange(0, 4))
