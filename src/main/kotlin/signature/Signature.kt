package signature

import java.math.BigInteger

data class Signature(val r: BigInteger, val s: BigInteger) {

    override fun toString() = "Signature(r=${r.toString(16)}, s=${s.toString(16)})"
}