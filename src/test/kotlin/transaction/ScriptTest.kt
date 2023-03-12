package transaction

import extension.decodeHex
import extension.toHex
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import script.Script

class ScriptTest : StringSpec({

    "should parse a ScriptPubKey" {
        val script = Script.parse(
            scriptPubKey.decodeHex()
                .inputStream()
        )
        (script.commands.component1() as ByteArray).toHex() shouldBe command1Expected.decodeHex().toHex()
        (script.commands.component2() as ByteArray).toHex() shouldBe command2Expected.decodeHex().toHex()
    }

    "should serialize a ScriptPubKey" {
        val script = Script.parse(
            scriptPubKey.decodeHex()
                .inputStream()
        )
        script.serialize().toHex() shouldBe scriptPubKey
    }

    "should evaluate a script with pubKey and a valid signature" {
        val z = "7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d".toBigInteger(16)
        val sec = ("04887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e" +
                "4da568744d06c61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34").decodeHex()
        val sig = ("3045022000eff69ef2b1bd93a66ed5219add4fb51e11a840f404876" +
                "325a1e8ffe0529a2c022100c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab601").decodeHex()
        val scriptSig = Script(createCommands(sig))
        val scriptPubKey = Script(createCommands(sec, OP_CHECKSIG))
        val combinedScript = scriptSig + scriptPubKey
        combinedScript.evaluate(z) shouldBe true
    }
})

private fun createCommands(vararg elements: Any): List<Any> {
    val list = mutableListOf<Any>()
    elements.forEach { list.add(it) }
    return list
}

val OP_CHECKSIG = 0xac

private const val scriptPubKey =
    "6a47304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d574" +
            "351872b7c361e9aae3649071c1a7160121035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937"

const val command1Expected =
    "304402207899531a52d59a6de200179928ca900254a36b8dff8bb75f5f5d71b1cdc26125022008b422690b8461cb52c3cc30330b23d5743518" +
            "72b7c361e9aae3649071c1a71601"

const val command2Expected = "035d5c93d9ac96881f19ba1f686f15f009ded7c62efe85a872e6a19b43c15a2937"
