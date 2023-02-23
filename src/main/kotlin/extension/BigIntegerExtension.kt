package extension

import ellipticcurve.N
import ellipticcurve.PointFieldElement
import ellipticcurve.S256Point
import java.math.BigInteger

operator fun BigInteger.times(point: PointFieldElement): PointFieldElement {
    val coef = this
    return multiplyPointAndCoef(point, coef)
}

operator fun BigInteger.times(point: S256Point): PointFieldElement {
    val coef = this.mod(N)
    return multiplyPointAndCoef(point, coef)
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
    var current = if (point is S256Point) point else point
    var result = if (point is S256Point) point.POINT_AT_INFINITY else point.POINT_AT_INFINITY
    while (coef1 != BigInteger.ZERO) {
        if (isRightMostBitIs1(coef1)) result += current
        current += current
        coef1 = coef1 shr 1
    }
    return result
}

private fun isRightMostBitIs1(coef: BigInteger) = (coef and 1.toBigInteger()) == 1.toBigInteger()

fun numberToHex(bigInteger: BigInteger?) = bigInteger?.toString(16)?.padStart(64, '0')
