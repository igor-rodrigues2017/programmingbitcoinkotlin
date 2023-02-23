package ellipticcurve

import extension.encodeBase58CheckSum
import extension.hash160
import extension.numberToHex
import finitefield.FieldElement
import java.math.BigInteger
import extension.times
import java.math.BigInteger.ZERO
import java.util.InvalidPropertiesFormatException

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

    private val testNetPrefix = byteArrayOf(0x6f.toByte())
    private val mainNetPrefix = byteArrayOf(0x00.toByte())
    fun address(compressed: Boolean = true, testNet: Boolean = false) =
        if (testNet) (testNetPrefix + hash160(sec(compressed))).encodeBase58CheckSum()
        else (mainNetPrefix + hash160(sec(compressed))).encodeBase58CheckSum()

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

    private fun isTangentLineIsVertical(other: PointFieldElement) = this == other && y!!.number == ZERO

    private fun validateIfPointsIsOnSameCurve(other: PointFieldElement) {
        if (a != other.a || b != other.b)
            throw UnsupportedOperationException("$this and $other are not on the same curve")
    }

    open val POINT_AT_INFINITY: PointFieldElement by lazy {
        getPointAtInfinity()
    }

    private fun getPointAtInfinity() = PointFieldElement(null, null, a, b)

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

    fun sec(compressed: Boolean = true): ByteArray = if (compressed) compressedSec() else uncompressedSec()

    private fun uncompressedSec() =
        byteArrayOf(0x04) + fix32Bytes(x!!.number.toByteArray()) + fix32Bytes(y!!.number.toByteArray())

    private fun compressedSec() = if (yIsEven())
        byteArrayOf(0x02) + fix32Bytes(x!!.number.toByteArray())
    else
        byteArrayOf(0x03) + fix32Bytes(x!!.number.toByteArray())

    private fun yIsEven() = y!!.number.mod(2.toBigInteger()) == ZERO

    private fun fix32Bytes(xBytes: ByteArray) = if (xBytes.size > 32) xBytes.copyOfRange(1, 33) else xBytes.copyOf(32)

    override fun toString() =
        if (this.x == null) "Point(infinity)"
        else "Point(${numberToHex(x.number)}," +
                " ${numberToHex(y?.number)})_" +
                "${numberToHex(a.number)}_" +
                "${this.b.number.toString(16).padStart(64, '0')} " +
                "FieldElement(${b.prime.toString(16).padStart(64, '0')})"

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
