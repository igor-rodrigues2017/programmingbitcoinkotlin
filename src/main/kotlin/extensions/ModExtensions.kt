package extensions

import java.math.BigInteger

fun BigInteger.floorMod(y: BigInteger): BigInteger {
    var mod = this % y
    // if the signs are different and modulo not zero, adjust result
    if ((mod.pow(y.toInt())) < BigInteger.ZERO && mod != BigInteger.ZERO) {
        mod += y
    }
    return mod
}

fun BigInteger.floorMod(y: Int): BigInteger {
    var mod = this % y.toBigInteger()
    // if the signs are different and modulo not zero, adjust result
    if ((mod.pow(y)) < BigInteger.ZERO && mod != BigInteger.ZERO) {
        mod += y.toBigInteger()
    }
    return mod
}