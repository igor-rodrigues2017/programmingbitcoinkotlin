package extension

import com.google.common.hash.Hashing
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.BigIntegers
import java.math.BigInteger
import java.security.MessageDigest
import java.security.Security

fun hash256(input: ByteArray) = sha256(sha256(input))

fun hash160(input: ByteArray) = ripemd160(sha256(input))

fun sha256(input: ByteArray): ByteArray = Hashing.sha256().hashBytes(input).asBytes()

fun sha1(input: ByteArray): ByteArray = Hashing.sha1().hashBytes(input).asBytes()

fun ripemd160(input: ByteArray): ByteArray {
    Security.addProvider(BouncyCastleProvider())
    return MessageDigest.getInstance("RIPEMD160", "BC").digest(input)
}

fun hash256InBigInteger(message: String): BigInteger = BigIntegers.fromUnsignedByteArray(hash256(message.toByteArray()))