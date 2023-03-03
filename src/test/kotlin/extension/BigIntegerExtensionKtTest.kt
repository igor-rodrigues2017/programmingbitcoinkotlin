package extension

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class BigIntegerExtensionKtTest : StringSpec({

    "should encode number in varint little endian" {
        forAll(
            row(
                15.toBigInteger(), "0f".decodeHex()
            ), row(
                300.toBigInteger(), "fd2c01".decodeHex()
            ), row(
                70_015.toBigInteger(), "fe7f1101".decodeHex()
            ), row(
                18_005_558_675_309.toBigInteger(), "ff6dc7ed3e6010".decodeHex()
            )
        ) { number, byteArray ->
            number.toVarint() shouldBe byteArray
        }
    }
})
