package extension

import com.google.common.hash.Hashing

fun hash256(input: ByteArray): ByteArray {
    return sha256(sha256(input))
}

private fun sha256(input: ByteArray): ByteArray = Hashing.sha256().hashBytes(input).asBytes()