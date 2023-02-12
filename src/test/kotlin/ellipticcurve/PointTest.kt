package ellipticcurve

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.InvalidPropertiesFormatException

class PointTest : StringSpec({
    val a = 5
    val b = 7

    "should validate if the point is on the curve" {
        shouldThrow<InvalidPropertiesFormatException> {
            Point(-1, -2, a, b)
        }
        shouldNotThrowAny {
            Point(-1, -1, a, b)
            Point(18, 77, a, b)
            Point(null, null, a, b)
        }
    }

    "should verify if sum points is in the same curve" {
        val aDifferent = 0
        val bDifferent = 0
        shouldThrow<UnsupportedOperationException> {
            Point(-1, -1, a, b) + Point(0, 0, a, bDifferent)
            Point(-1, -1, a, b) + Point(0, 0, aDifferent, b)
        }

    }

    "should addition point and point at infinity (identity point)" {
        val x = -1
        val p1 = Point(x, -1, a, b)
        val p2 = Point(x, 1, a, b)
        val infinity = Point(null, null, a, b)

        p1 + infinity shouldBe p1
        p2 + infinity shouldBe p2
    }

    "should addition inverses points (x coordinate is equals)" {
        val x = -1
        val infinity = Point(null, null, a, b)

        Point(x, -1, a, b) + Point(x, 1, a, b) shouldBe infinity
    }

    "should addition when x is different" {
        Point(2, 5, a, b) + Point(-1, -1, a, b) shouldBe Point(3, -7, a, b)
    }

    "should addition when P1 = P2" {
        val point = Point(-1, -1, a, b)
        point + point shouldBe Point(18, 77, a, b)
    }

    "should addition when P1 = P2 and Coordinate y is ZERO" {
        val point =  Point(0, 0, 0, 0)
        val infinity = Point(null, null, 0, 0)
        point + point shouldBe infinity
    }
})