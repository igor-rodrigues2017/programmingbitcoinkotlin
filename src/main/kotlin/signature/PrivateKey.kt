package signature

import ellipticcurve.G
import ellipticcurve.N
import extension.hash256InBigInteger
import extension.invertFermatTheorem
import extension.times
import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class PrivateKey(private val e: BigInteger) {

    constructor(secret: String) : this(hash256InBigInteger(secret))

    val publicKey = e * G

    override fun toString() = e.toString(16).padStart(64, '0')

    fun sign(message: String): Signature {
        val z = hash256InBigInteger(message)
        val k = deterministicK(z)
        val r = getXCoordinateOfThePoint(k)
        val s = ((z + r * e) * k.invertFermatTheorem()).mod(N)
        return if (mostBiggerThanN(s)) Signature(r, s = N - s) else Signature(r, s)
    }

    /**
     * It turns out that using the low-s value will get nodes to relay our transactions.
     * This is for malleability reasons.
     */
    private fun mostBiggerThanN(s: BigInteger) = s > N / 2.toBigInteger()

    private fun getXCoordinateOfThePoint(k: BigInteger) = (k * G).x!!.number

    /**
     * The specification is in RFC 6979
     */
    private fun deterministicK(z: BigInteger): BigInteger {
        var k = ByteArray(32)
        var v = ByteArray(32) { 1 }
        var zVal = z
        if (z > N) zVal -= N
        var zBytes = zVal.toByteArray()
        if (zBytes.size < 32) zBytes = ByteArray(32 - zBytes.size) + zBytes
        var secretBytes = e.toByteArray()
        if (secretBytes.size < 32) secretBytes = ByteArray(32 - secretBytes.size) + secretBytes
        k = hmac(k, v + byteArrayOf(0x00.toByte()) + secretBytes + zBytes)
        v = hmac(k, v)
        k = hmac(k, v + byteArrayOf(0x01.toByte()) + secretBytes + zBytes)
        v = hmac(k, v)
        while (true) {
            v = hmac(k, v)
            val candidate = BigInteger(1, v)
            if (candidate >= BigInteger.ONE && candidate < N) return candidate
            k = hmac(k, v + byteArrayOf(0x00.toByte()))
            v = hmac(k, v)
        }
    }

    private fun hmac(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(SecretKeySpec(key, "HmacSHA256"))
        return hmac.doFinal(data)
    }
}
