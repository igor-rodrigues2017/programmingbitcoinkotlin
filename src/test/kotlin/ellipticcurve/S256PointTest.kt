package ellipticcurve

import extension.decodeHex
import extension.toHex
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import signature.PrivateKey
import signature.Signature

class S256PointTest : StringSpec({

    val uncompressedPrefix = "04"
    val odd = "03"
    val even = "02"

    "should verify if the signature is valid" {
        forAll(
            row(point, hashA, signatureHashA, true),
            row(point, hashB, signatureHashB, true),
            row(point, hashA, signatureHashB, false)
        ) { publicKey, hash, signature, expected ->
            publicKey.verify(hash, signature) shouldBe expected
        }
    }

    "should convert the point to uncompressed SEC format" {
        forAll(
            row(
                PrivateKey(5000.toBigInteger()),
                "ffe558e388852f0120e46af2d1b370f85854a8eb0841811ece0e3e03d282d57c",
                "315dc72890a4f10a1481c031b03b351b0dc79901ca18a00cf009dbdb157a1d10"
            ),
            row(
                PrivateKey(2018.toBigInteger().pow(5)),
                "027f3da1918455e03c46f659266a1bb5204e959db7364d2f473bdf8f0a13cc9d",
                "ff87647fd023c13b4a4994f17691895806e1b40b57f4fd22581a4f46851f3b06"
            ),
            row(
                PrivateKey(0xdeadbeef12345.toBigInteger()),
                "d90cd625ee87dd38656dd95cf79f65f60f7273b67d3096e68bd81e4f5342691f",
                "842efa762fd59961d0e99803c61edba8b3e3f7dc3a341836f97733aebf987121"
            )
        ) { privateKey, x, y ->
            privateKey.publicKey.sec(compressed = false).toHex() shouldBe "$uncompressedPrefix$x$y"
        }
    }

    "should convert the point to compressed SEC format" {
        forAll(
            row(
                PrivateKey(5001.toBigInteger()),
                "${odd}57a4f368868a8a6d572991e484e664810ff14c05c0fa023275251151fe0e53d1",
            ),
            row(
                PrivateKey(2019.toBigInteger().pow(5)),
                "${even}933ec2d2b111b92737ec12f1c5d20f3233a0ad21cd8b36d0bca7a0cfa5cb8701",
            ),
            row(
                PrivateKey(0xdeadbeef54321.toBigInteger()),
                "${even}96be5b1292f6c856b3c5654e886fc13511462059089cdf9c479623bfcbe77690",
            )
        ) { privateKey, expected ->
            privateKey.publicKey.sec().toHex() shouldBe expected
        }
    }

    "should parse an uncompressed and compressed SEC format to a point" {
        forAll(
            row(
                ("${uncompressedPrefix}ffe558e388852f0120e46af2d1b370f85854a8eb0841811ece0e3e03d282d57c" +
                        "315dc72890a4f10a1481c031b03b351b0dc79901ca18a00cf009dbdb157a1d10").decodeHex(),
                PrivateKey(5000.toBigInteger()).publicKey
            ),
            row(
                ("${odd}57a4f368868a8a6d572991e484e664810ff14c05c0fa023275251151fe0e53d1").decodeHex(),
                PrivateKey(5001.toBigInteger()).publicKey
            ),
            row(
                ("${even}96be5b1292f6c856b3c5654e886fc13511462059089cdf9c479623bfcbe77690").decodeHex(),
                PrivateKey(0xdeadbeef54321.toBigInteger()).publicKey
            )
        ) { secFormat, point ->
            S256Point.parse(secFormat) shouldBe point
        }
    }

    "should generate bitcoin addresses from serialized" {
        forAll(
            row(
                PrivateKey(5002.toBigInteger()),
                false,
                true,
                "mmTPbXQFxboEtNRkwfh6K51jvdtHLxGeMA"
            ),
            row(
                PrivateKey(2020.toBigInteger().pow(5)),
                true,
                true,
                "mopVkxp8UhXqRYbCYJsbeE1h1fiF64jcoH"
            ),
            row(
                PrivateKey(0x12345deadbeef.toBigInteger()),
                true,
                false,
                "1F1Pn2y6pDb68E5nYJJeba4TLg2U7B6KF1"
            )
        ) { privateKey, compressed, testNet, address ->
            privateKey.publicKey.address(compressed, testNet) shouldBe address
        }
    }

    "should generate bitcoin addresses on main net and compressed by default from serialized" {
        PrivateKey(0x12345deadbeef.toBigInteger()).publicKey.address() shouldBe "1F1Pn2y6pDb68E5nYJJeba4TLg2U7B6KF1"
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
