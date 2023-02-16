package signature

import ellipticcurve.G
import ellipticcurve.N
import extension.hash256
import extension.invertFermatTheorem
import extension.times
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class PrivateKeyTest : StringSpec({

    val correctedMessage = "Programming Bitcoin!"
    val wrongMessage = "Wrong Message!"
    val secret = "my secret"
    val privateKey = PrivateKey(secret)

    "should signing correctly a message" {
        val signature = privateKey.sign(correctedMessage)

        isValidSignature(correctedMessage, privateKey, signature) shouldBe true
        isValidSignature(wrongMessage, privateKey, signature) shouldBe false
    }

    "should create deterministic sign" {
        privateKey.sign(correctedMessage) shouldBe Signature(
            r = "3b8293530687fe7b47ea6f824431261f45bdc0e92bc865a2d4006ff599339af8".toBigInteger(16),
            s = "301c56693fc4b3043e06445697a77aecec175667a4b91b4d53ff5e78bcdb31fc".toBigInteger(16)
        )
    }
})

private fun isValidSignature(message: String, privateKey: PrivateKey, signature: Signature): Boolean {
    val sInv = signature.s.invertFermatTheorem()
    val z = BigInteger(1, hash256(message.toByteArray()))
    val u = (z * sInv).mod(N)
    val v = (signature.r * sInv).mod(N)
    return (u * G + v * privateKey.publicKey).x?.number == signature.r
}
