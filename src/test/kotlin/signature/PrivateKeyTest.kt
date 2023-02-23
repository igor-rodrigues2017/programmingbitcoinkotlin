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
            r = "4b0d525a4e225ac5dbb2796a9e23c78a011934a844da9cc0ebe34e94ec2d7fbb".toBigInteger(16),
            s = "7991ee368872acf4dc18f9a212a8069d7e62e21d461eadf843b84fecb652d880".toBigInteger(16)
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
