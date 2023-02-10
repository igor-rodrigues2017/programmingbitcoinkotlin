package finitefield

import extensions.floorMod
import java.math.BigInteger
import java.math.BigInteger.ZERO
import java.util.*

data class FieldElement(
    val number: BigInteger,
    val prime: BigInteger
) {

    constructor(number: Int, prime: Int) : this(number.toBigInteger(), prime.toBigInteger())

    init {
        if (number >= prime || number < ZERO)
            throw InvalidPropertiesFormatException("Invalid Number")
    }

    operator fun plus(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(sum(other), prime)
    }

    private fun sum(other: FieldElement) = (this.number + other.number).floorMod(prime)

    operator fun minus(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(sub(other), prime)
    }

    private fun sub(other: FieldElement) = (this.number - other.number).floorMod(prime)

    operator fun times(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(multiply(other), prime)
    }

    private fun multiply(other: FieldElement) = (number * other.number).floorMod(prime)

    fun pow(exp: BigInteger): FieldElement = FieldElement(exponentiation(exp), prime)

    fun pow(exp: Int): FieldElement = FieldElement(exponentiation(exp.toBigInteger()), prime)

    private fun exponentiation(exp: BigInteger) = number.modPow(exp, prime)

    private fun validIsAnElementOfField(other: FieldElement) {
        if (this.prime != other.prime)
            throw UnsupportedOperationException("Cannot make this operations with two numbers in different Fields")
    }

    operator fun div(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(divide(other), prime)
    }

    private fun divide(other: FieldElement) = (number * invertFermatTheorem(other.number)).floorMod(prime)

    private fun invertFermatTheorem(number: BigInteger) = number.modPow(prime - 2.toBigInteger(), prime)
}