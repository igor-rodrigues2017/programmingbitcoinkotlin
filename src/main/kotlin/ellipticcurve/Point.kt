package ellipticcurve

import java.math.BigInteger
import java.math.BigInteger.ZERO
import java.util.InvalidPropertiesFormatException

/**
 * Represent a Point in elliptic curve
 *
 * equation: y² = x³ + a ⋅ x + b
 */
data class Point(
    val x: BigInteger?,
    val y: BigInteger?,
    val a: BigInteger,
    val b: BigInteger
) {

    constructor(x: Int?, y: Int?, a: Int, b: Int) : this(
        x?.toBigInteger(),
        y?.toBigInteger(),
        a.toBigInteger(),
        b.toBigInteger()
    )

    init {
        if (x != null && y != null)
            if (pointIsNotOnTheCurve(y, x))
                throw InvalidPropertiesFormatException("($x, $y) is not on the curve")
    }

    private fun pointIsNotOnTheCurve(y: BigInteger, x: BigInteger) = y.pow(2) != x.pow(3) + a * x + b

    operator fun plus(other: Point): Point {
        validateIfPointsIsOnSameCurve(other)
        return when {
            this == POINT_AT_INFINITY -> other
            other == POINT_AT_INFINITY -> this
            isAdditiveInverses(other) || isTangentLineIsVertical(other) -> POINT_AT_INFINITY
            x != other.x -> calculateAdditiveWhenCoordinateIsDifferent(other)
            this == other -> calculateAdditiveWhenTangentToTheCurve(other)
            else -> throw UnsupportedOperationException()
        }
    }

    private fun isTangentLineIsVertical(other: Point) = this == other && y == ZERO

    private fun validateIfPointsIsOnSameCurve(other: Point) {
        if (a != other.a || b != other.b)
            throw UnsupportedOperationException("$this and $other are not on the same curve")
    }

    private val POINT_AT_INFINITY: Point by lazy {
        getPointAtInfinity()
    }

    private fun getPointAtInfinity() = Point(null, null, a, b)

    private fun isAdditiveInverses(other: Point) = x == other.x && y != other.y

    /**
     * Additive when coordinate x are different:
     *
     * s = (y2 – y1)/(x2 – x1)
     *
     * x3 = s² – x1 – x2
     *
     * y3 = s(x1 – x3) – y1
     */
    private fun calculateAdditiveWhenCoordinateIsDifferent(other: Point): Point {
        val s = calculateSlopeToDifferentPoints(other)
        val x = calculateXCoordinate(s, other.x!!)
        val y = calculateYCoordinate(s, x)
        return Point(x, y, a, b)
    }

    /**
     * Additive when P1 = P2:
     * tangent to the curve at point
     *
     * s = (3x1² + a)/(2y1)
     *
     * x3 = s² – 2x1
     *
     * y3 = s(x1 – x3) – y1
     */
    private fun calculateAdditiveWhenTangentToTheCurve(other: Point): Point {
        val s = calculateSlopeToEqualsPoints()
        val x = calculateXCoordinate(s, other.x!!)
        val y = calculateYCoordinate(s, x)
        return Point(x, y, a, b)
    }

    private fun calculateSlopeToDifferentPoints(other: Point) = (other.y!! - y!!) / (other.x!! - x!!)

    private fun calculateSlopeToEqualsPoints() = (3.toBigInteger() * x!!.pow(2) + a) / (2.toBigInteger() * y!!)

    private fun calculateXCoordinate(s: BigInteger, otherX: BigInteger) = s.pow(2) - x!! - otherX

    private fun calculateYCoordinate(s: BigInteger, calculatedX: BigInteger) = s * (x!! - calculatedX) - y!!

}
