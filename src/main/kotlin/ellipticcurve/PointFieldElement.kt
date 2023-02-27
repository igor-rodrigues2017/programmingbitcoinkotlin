package ellipticcurve

import extension.times
import extension.toHex64
import finitefield.FieldElement
import java.math.BigInteger
import java.math.BigInteger.ZERO
import java.util.*

/**
 * Represent a Point in elliptic curve
 *
 * equation: y² = x³ + a ⋅ x + b
 */
open class PointFieldElement(
    val x: FieldElement?,
    val y: FieldElement?,
    val a: FieldElement,
    val b: FieldElement
) {

    init {
        if (x != null && y != null)
            if (pointIsNotOnTheCurve(y, x))
                throw InvalidPropertiesFormatException("($x, $y) is not on the curve")
    }

    private fun pointIsNotOnTheCurve(y: FieldElement, x: FieldElement) = y.pow(2) != x.pow(3) + (a * x) + b

    open val POINT_AT_INFINITY: PointFieldElement by lazy {
        getPointAtInfinity()
    }

    private fun getPointAtInfinity() = PointFieldElement(null, null, a, b)

    operator fun times(other: BigInteger): PointFieldElement {
        return other * this
    }

    operator fun plus(other: PointFieldElement): PointFieldElement {
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

    private fun validateIfPointsIsOnSameCurve(other: PointFieldElement) {
        if (a != other.a || b != other.b)
            throw UnsupportedOperationException("$this and $other are not on the same curve")
    }

    private fun isTangentLineIsVertical(other: PointFieldElement) = this == other && y!!.number == ZERO

    private fun isAdditiveInverses(other: PointFieldElement) = x == other.x && y != other.y

    /**
     * Additive when coordinate x are different:
     *
     * s = (y2 – y1)/(x2 – x1)
     *
     * x3 = s² – x1 – x2
     *
     * y3 = s(x1 – x3) – y1
     */
    private fun calculateAdditiveWhenCoordinateIsDifferent(other: PointFieldElement): PointFieldElement {
        val s = calculateSlopeToDifferentPoints(other)
        val x = calculateXCoordinate(s, other.x!!)
        val y = calculateYCoordinate(s, x)
        return PointFieldElement(x, y, a, b)
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
    private fun calculateAdditiveWhenTangentToTheCurve(other: PointFieldElement): PointFieldElement {
        val s = calculateSlopeToEqualsPoints()
        val x = calculateXCoordinate(s, other.x!!)
        val y = calculateYCoordinate(s, x)
        return PointFieldElement(x, y, a, b)
    }

    private fun calculateSlopeToDifferentPoints(other: PointFieldElement) = (other.y!! - y!!) / (other.x!! - x!!)

    private fun calculateSlopeToEqualsPoints(): FieldElement {
        val tree = FieldElement(3.toBigInteger(), x!!.prime)
        val two = FieldElement(2.toBigInteger(), x.prime)
        return (tree * x.pow(2) + a) / (two * y!!)
    }

    private fun calculateXCoordinate(s: FieldElement, otherX: FieldElement) = s.pow(2) - x!! - otherX

    private fun calculateYCoordinate(s: FieldElement, calculatedX: FieldElement) = s * (x!! - calculatedX) - y!!

    override fun toString() =
        if (this.x == null) "Point(infinity)"
        else "Point(${x.number.toHex64()}," +
                " ${y?.number?.toHex64()})_" +
                "${a.number.toHex64()}_" +
                "${this.b.number.toHex64()} " +
                "FieldElement(${b.prime.toHex64()})"

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is PointFieldElement ->
                this.x == other.x &&
                        this.y == other.y &&
                        this.a == other.a &&
                        this.b == other.b

            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = x?.hashCode() ?: 0
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }

}
