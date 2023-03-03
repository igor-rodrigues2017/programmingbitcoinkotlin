package transaction

import extension.decodeHex
import extension.toHex
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ScriptTest : StringSpec({

    "should parse a ScriptPubKey" {
        val script = Script.parse(
            scriptPubKey.decodeHex()
                .inputStream()
        )
        (script.commands.component1() as ByteArray).toHex() shouldBe command1Expected.decodeHex().toHex()
        (script.commands.component2() as ByteArray).toHex() shouldBe command2Expected.decodeHex().toHex()
    }
})

private const val scriptPubKey =
    "6a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574" +
            "351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937"

val command1Expected =
    "304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d5743518" +
            "72b7c361e9aae3649071c1a71601"

val command2Expected = "035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937"
