package finitefield

import java.math.BigInteger

val prime = (2.toBigInteger().pow(256)) - (2.toBigInteger().pow(32)) - (977.toBigInteger())

class S256Field(number: BigInteger) : FieldElement(number = number, prime = prime) {
    override fun toString() = number.toString(16).padStart(64, '0')
}