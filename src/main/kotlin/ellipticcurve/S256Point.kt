package ellipticcurve

import extension.times
import finitefield.FieldElement
import finitefield.S256Field
import finitefield.PRIMES256
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

        private fun isUncompressed(secBin: ByteArray) = secBin[0] == 4.toByte()

        private fun calculateRightSideOfEquation(x: S256Field) = x.pow(3) + S256Field(B)

        private fun determineEvenness(beta: FieldElement) = if (isEven(beta)) {
            Pair(beta, S256Field(PRIMES256 - beta.number))
        } else {
            Pair(S256Field(PRIMES256 - beta.number), beta)
        }

        private fun isEven(beta: FieldElement) = beta.number % 2.toBigInteger() == 0.toBigInteger()

        private fun yIsEven(secBin: ByteArray) = secBin[0] == 2.toByte()
    }

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

    private fun divide(hash: BigInteger, sInv: BigInteger): BigInteger =
        (hash * sInv).mod(N)

    private fun invertSFermatTheorem(bigInteger: BigInteger): BigInteger =
        bigInteger.modPow(N - 2.toBigInteger(), N)
}