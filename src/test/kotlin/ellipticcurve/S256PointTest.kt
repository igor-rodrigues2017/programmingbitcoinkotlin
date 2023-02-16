package ellipticcurve

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import signature.Signature

class S256PointTest : StringSpec({

    "should verify if the signature is valid" {
        forAll(
            row(point, hashA, signatureHashA, true),
            row(point, hashB, signatureHashB, true),
            row(point, hashA, signatureHashB, false)
        ) { publicKey, hash, signature, expected ->
            publicKey.verify(hash, signature) shouldBe expected
        }
    }
})

private val point = S256Point(
    x = "887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c".toBigInteger(16),
    y = "61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34".toBigInteger(16)
)
private val hashA = "ec208baa0fc1c19f708a9ca96fdeff3ac3f230bb4a7ba4aede4942ad003c0f60".toBigInteger(16)
private val hashB = "7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d".toBigInteger(16)
private val signatureHashA = Signature(
    r = "ac8d1c87e51d0d441be8b3dd5b05c8795b48875dffe00b7ffcfac23010d3a395".toBigInteger(16),
    s = "68342ceff8935ededd102dd876ffd6ba72d6a427a3edb13d26eb0781cb423c4".toBigInteger(16)
)
private val signatureHashB = Signature(
    r = "eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c".toBigInteger(16),
    s = "c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6".toBigInteger(16)
)
