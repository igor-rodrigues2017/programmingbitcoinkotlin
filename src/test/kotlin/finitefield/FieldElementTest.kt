package finitefield

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import java.util.InvalidPropertiesFormatException

class FieldElementTest : StringSpec({

    "should throw an exception when number isn't between ZERO and prime - 1 inclusive" {
        shouldThrow<InvalidPropertiesFormatException> {
            FieldElement(6, 5)
        }
    }

    "should verify if field elements are equals" {
        val fieldA = FieldElement(7, 13)
        val fieldB = FieldElement(12, 13)
        (fieldA == fieldB) shouldBe false
        (fieldA == fieldA) shouldBe true
    }

    "should throw an exception when try sum two numbers in different Fields" {
        shouldThrow<UnsupportedOperationException> {
            FieldElement(7, 17) + FieldElement(12, 13)
        }
    }

    "should sum field elements" {
        FieldElement(7, 13) + FieldElement(12, 13) shouldBe FieldElement(6, 13)
    }

    "should throw an exception when try sub two numbers in different Fields" {
        shouldThrow<UnsupportedOperationException> {
            FieldElement(7, 17) - FieldElement(12, 13)
        }
    }

    "should sub field elements" {
        FieldElement(6, 19) - FieldElement(13, 19) shouldBe
                FieldElement(12, 19)
    }

    "should throw an exception when try multiply two numbers in different Fields" {
        shouldThrow<UnsupportedOperationException> {
            FieldElement(7, 17) * FieldElement(12, 13)
        }
    }

    "should multiply field elements" {
        FieldElement(3, 13) * FieldElement(12, 13) shouldBe
                FieldElement(10, 13)
    }

    "should exponentiation field elements" {
        FieldElement(3, 13).pow(3) shouldBe FieldElement(1, 13)
        FieldElement(7, 13).pow(-3) shouldBe FieldElement(8, 13)
        FieldElement(7, 13).pow(BigInteger.valueOf(10000000000000000)) shouldBe FieldElement(9, 13)
    }

    "should throw an exception when try divide two numbers in different Fields" {
        shouldThrow<UnsupportedOperationException> {
            FieldElement(7, 17) / FieldElement(12, 13)
        }
    }

    "should divide field elements" {
        FieldElement(3, 31) / FieldElement(24, 31) shouldBe FieldElement(4, 31)
    }
})