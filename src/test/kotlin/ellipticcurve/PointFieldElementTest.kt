package ellipticcurve

import finitefield.FieldElement
import extension.times
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import java.util.*

class PointFieldElementTest : StringSpec({

    "should validate if the point is not on the curve" {
        forAll(
            row(200.toBigInteger(), 119.toBigInteger()),
            row(42.toBigInteger(), 99.toBigInteger())
        ) { xRaw, yRaw ->
            val x = FieldElement(xRaw, prime)
            val y = FieldElement(yRaw, prime)
            shouldThrow<InvalidPropertiesFormatException> {
                PointFieldElement(x, y, zero, seven)
            }
        }
    }

    "should validate if the point is on the curve" {
        forAll(
            row(192.toBigInteger(), 105.toBigInteger()),
            row(17.toBigInteger(), 56.toBigInteger()),
            row(1.toBigInteger(), 193.toBigInteger())
        ) { xRaw, yRaw ->
            val x = FieldElement(xRaw, prime)
            val y = FieldElement(yRaw, prime)
            shouldNotThrow<InvalidPropertiesFormatException> {
                PointFieldElement(x, y, zero, seven)
            }
        }
    }

    "should sum the points" {
        forAll(
            row(
                aPoint(192, 105),
                aPoint(17, 56),
                aPoint(170, 142)
            ),row(
                aPoint(170, 142),
                aPoint(60, 139),
                aPoint(220,181)
            ),row(
                aPoint(47, 71),
                aPoint(17, 56),
                aPoint(215,68)
            ),
            row(
                aPoint(143, 98),
                aPoint(76, 66),
                aPoint(47,71)
            )
        ) { p1, p2, expected ->
            p1 + p2 shouldBe expected
        }
    }

    "should multiply a point" {
        val point = aPoint(15, 86)
        val point2 = aPoint(47, 71)
        7.toBigInteger() * point shouldBe point.POINT_AT_INFINITY

        2.toBigInteger() * point2 shouldBe  aPoint(36, 111)
    }

})

private val prime = 223.toBigInteger()
private val zero = FieldElement(BigInteger.ZERO, prime)
private val seven = FieldElement(7.toBigInteger(), prime)

private fun aPoint(
    x: Int,
    y: Int
) = PointFieldElement(
    x = FieldElement(x.toBigInteger(), prime),
    y = FieldElement(y.toBigInteger(), prime),
    zero,
    seven
)
