package extension

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class ByteArrayExtensionKtTest : StringSpec({

    "should encode byteArray to base58" {
        forAll(
            row(
                "7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d",
                "9MA8fRQrT4u8Zj8ZRd6MAiiyaxb2Y1CMpvVkHQu5hVM6"
            ), row(
                "eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c",
                "4fE3H2E6XMp4SsxtwinF7w9a34ooUrwWe4WsW1458Pd"
            ), row(
                "c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6",
                "EQJsjkd6JaGwxrjEhfeqPenqHwrBmPQZjJGNSCHBkcF7"
            )
        ) { hex, base58 ->
            hex.decodeHex().encodeBase58() shouldBe base58
        }
    }

    "should decode base58 to number" {
        forAll(
            row(
                "mnrVtF8DWjMu839VW3rBfgYaAfKk8983Xf",
                "507b27411ccf7f16f10297de6cef3f291623eddf"
            )
        ) { addr, hash160 ->
            addr.decodeBase58().toHex() shouldBe hash160
            (byteArrayOf(0x6f.toByte()) + hash160.decodeHex()).encodeBase58CheckSum() shouldBe addr
        }
    }

    "should read varint in little endian and return a BigInteger" {
        forAll(
            row(
                "01".decodeHex().inputStream(), BigInteger.ONE
            ), row(
                "fd2c0100".decodeHex().inputStream(), 300.toBigInteger()
            ), row(
                "fe7f110100".decodeHex().inputStream(), 70_015.toBigInteger()
            ), row(
                "ff6dc7ed3e601000".decodeHex().inputStream(), 18_005_558_675_309.toBigInteger()
            )
        ) { inputStream, numberExpected ->
            inputStream.readVarint() shouldBe numberExpected
        }
    }

})
