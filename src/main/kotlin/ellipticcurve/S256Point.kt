package ellipticcurve

import extension.encodeBase58CheckSum
import extension.hash160
import extension.times
import extension.to32ByteArray
import finitefield.FieldElement
import finitefield.PRIMES256
import finitefield.S256Field
import signature.Signature
import java.math.BigInteger

val A = BigInteger.ZERO
val B = 7.toBigInteger()
val N = "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141".toBigInteger(16)
val G = S256Point(
    "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798".toBigInteger(16),
    "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8".toBigInteger(16)
)

class S256Point(
    x: FieldElement?,
    y: FieldElement?
) : PointFieldElement(
    x = x,
    y = y,
    a = S256Field(A),
    b = S256Field(B)
) {
    constructor(x: BigInteger, y: BigInteger) : this(S256Field(x), S256Field(y))

    companion object {

        private val TEST_NET_PREFIX = byteArrayOf(0x6f.toByte())
        private val MAIN_NET_PREFIX = byteArrayOf(0x00.toByte())
        private val UNCOMPRESSED_SEC_PREFIX = byteArrayOf(0x04)
        private val COMPRESSED_SEC_EVEN_PREFIX = byteArrayOf(0x02)
        private val COMPRESSED_SEC_ODD_PREFIX = byteArrayOf(0x03)

        /**
         * equation y² = x³ + 7
         */
        fun parse(secBin: ByteArray): S256Point = if (isUncompressed(secBin)) {
            S256Point(
                x = BigInteger(1, secBin.copyOfRange(1, 33)),
                y = BigInteger(1, secBin.copyOfRange(33, 65))
            )
        } else {
            val x = S256Field(BigInteger(1, secBin.copyOfRange(1, secBin.size)))
            val y = calculateRightSideOfEquation(x).sqrt()
            val (evenBeta, oddBeta) = determineEvenness(y)
            if (yIsEven(secBin)) {
                S256Point(x, evenBeta)
            } else {
                S256Point(x, oddBeta)
            }
        }

        private fun isUncompressed(secBin: ByteArray) = secBin[0] == UNCOMPRESSED_SEC_PREFIX.component1()

        private fun calculateRightSideOfEquation(x: S256Field) = x.pow(3) + S256Field(B)

        private fun determineEvenness(y: FieldElement) = if (isEven(y)) {
            Pair(y, S256Field(PRIMES256 - y.number))
        } else {
            Pair(S256Field(PRIMES256 - y.number), y)
        }

        private fun isEven(beta: FieldElement) = beta.number % 2.toBigInteger() == 0.toBigInteger()

        private fun yIsEven(secBin: ByteArray) = secBin[0] == 2.toByte()
    }

    fun address(compressed: Boolean = true, testNet: Boolean = false) =
        if (testNet) (TEST_NET_PREFIX + hash160(sec(compressed))).encodeBase58CheckSum()
        else (MAIN_NET_PREFIX + hash160(sec(compressed))).encodeBase58CheckSum()

    /**
     * u = z/s
     * v = r/s
     * uG + vP = (r,y)
     */
    fun verify(hash: BigInteger, signature: Signature): Boolean {
        val sInv = invertSFermatTheorem(signature.s)
        val u = divide(hash, sInv)
        val v = divide(signature.r, sInv)
        val pointR = u * G + v * this
        return pointR.x?.number == signature.r
    }

    private fun invertSFermatTheorem(bigInteger: BigInteger): BigInteger =
        bigInteger.modPow(N - 2.toBigInteger(), N)

    private fun divide(hash: BigInteger, sInv: BigInteger): BigInteger =
        (hash * sInv).mod(N)

    fun sec(compressed: Boolean = true): ByteArray = if (compressed) compressedSec() else uncompressedSec()

    private fun uncompressedSec(): ByteArray {
        return UNCOMPRESSED_SEC_PREFIX + x!!.number.to32ByteArray() + y!!.number.to32ByteArray()
    }

    private fun compressedSec() = if (yIsEven()) {
        COMPRESSED_SEC_EVEN_PREFIX + x!!.number.to32ByteArray()
    } else
        COMPRESSED_SEC_ODD_PREFIX + x!!.number.to32ByteArray()

    private fun yIsEven() = y!!.number.mod(2.toBigInteger()) == BigInteger.ZERO
}