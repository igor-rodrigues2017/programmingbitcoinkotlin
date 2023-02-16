package finitefield

import extension.invertFermatTheorem
import java.math.BigInteger
import java.math.BigInteger.ZERO
import java.util.*

open class FieldElement(
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

    private fun sum(other: FieldElement) = (this.number + other.number).mod(prime)

    operator fun minus(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(sub(other), prime)
    }

    private fun sub(other: FieldElement) = (this.number - other.number).mod(prime)

    operator fun times(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(multiply(other), prime)
    }

    private fun multiply(other: FieldElement) = (number * other.number).mod(prime)

    fun pow(exp: BigInteger): FieldElement = FieldElement(number.modPow(exp, prime), prime)

    fun pow(exp: Int): FieldElement = FieldElement(number.modPow(exp.toBigInteger(), prime), prime)

    private fun validIsAnElementOfField(other: FieldElement) {
        if (this.prime != other.prime)
            throw UnsupportedOperationException("Cannot make this operations with two numbers in different Fields")
    }

    operator fun div(other: FieldElement): FieldElement {
        validIsAnElementOfField(other)
        return FieldElement(divide(other), prime)
    }

    private fun divide(other: FieldElement) = (number * other.number.invertFermatTheorem(prime)).mod(prime)

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is FieldElement -> this.number == other.number && this.prime == other.prime
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = number.hashCode()
        result = 31 * result + prime.hashCode()
        return result
    }


}