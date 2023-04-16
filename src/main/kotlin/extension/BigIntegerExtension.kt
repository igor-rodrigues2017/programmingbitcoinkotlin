package extension

import ellipticcurve.N
import ellipticcurve.PointFieldElement
import ellipticcurve.S256Point
import org.bouncycastle.util.BigIntegers
import java.math.BigInteger

operator fun BigInteger.times(point: PointFieldElement): PointFieldElement {
    val coefficient = this
    return multiplyPointAndCoef(point, coefficient)
}

operator fun BigInteger.times(point: S256Point): S256Point {
    val coefficient = this.mod(N)
    val pointFieldElement = multiplyPointAndCoef(point, coefficient)
    return S256Point(pointFieldElement.x, pointFieldElement.y)
}

/**
 * Default value to prime = N.
 * @see S256Point
 */
fun BigInteger.invertFermatTheorem(prime: BigInteger = N): BigInteger =
    this.modPow(prime - 2.toBigInteger(), prime)

private fun multiplyPointAndCoef(
    point: PointFieldElement,
    coef: BigInteger
): PointFieldElement {
    var coef1 = coef
    var current = point
    var result = point.POINT_AT_INFINITY
    while (coef1 != BigInteger.ZERO) {
        if (isRightMostBitIs1(coef1)) result += current
        current += current
        coef1 = coef1 shr 1
    }
    return result
}

private fun isRightMostBitIs1(coef: BigInteger) = (coef and 1.toBigInteger()) == 1.toBigInteger()

fun BigInteger.toHex64() = this.toString(16).padStart(64, '0')

fun BigInteger.to32ByteArray(): ByteArray = BigIntegers.asUnsignedByteArray(32, this)

fun BigInteger.to25ByteArray(): ByteArray = BigIntegers.asUnsignedByteArray(25, this)

fun BigInteger.toLittleEndianByteArray(): ByteArray = BigIntegers.asUnsignedByteArray(this).reversedArray()

fun BigInteger.toVarint(): ByteArray {
    return when {
        this < 0xfd.toBigInteger() -> this.toByteArray()
        this < 10_000.toBigInteger() -> byteArrayOf(VARINT_FD) + this.toLittleEndianByteArray()
        this < 100_000_000.toBigInteger() -> byteArrayOf(VARINT_FE) + this.toLittleEndianByteArray()
        this < 10_000_000_000_000_000.toBigInteger() -> byteArrayOf(VARINT_FF) + this.toLittleEndianByteArray()
        else -> throw IllegalArgumentException("integer too large: $this")
    }
}