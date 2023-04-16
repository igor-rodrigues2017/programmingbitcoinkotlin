package signature

import extension.decodeHex
import extension.toHex
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SignatureTest : StringSpec({

    "should serialize signature to Distinguished Encoding Rules (DRE)" {
        val marker = 30
        val sigLength = 45
        val rMarker = "02"
        val rLength = 20
        val sMarker = "02"
        val sLength = 21
        Signature(
            r = "37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6".toBigInteger(16),
            s = "8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec".toBigInteger(16)
        ).der().toHex() shouldBe "$marker" +
                "${sigLength}${rMarker}${rLength}" +
                "37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6" +
                "${sMarker}${sLength}" +
                "008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec"
    }

    "should parse a Distinguished Encoding Rules (DRE) format to a Signature" {
        Signature.parse(
            ("3045022037206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c60221008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec").decodeHex()
        ) shouldBe Signature(
            r = "37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6".toBigInteger(16),
            s = "8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec".toBigInteger(16)
        )
    }

})


