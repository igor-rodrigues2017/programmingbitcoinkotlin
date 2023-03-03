package transaction

import extension.littleEndianToBigInteger
import extension.toLittleEndianByteArray
import java.io.ByteArrayInputStream
import java.math.BigInteger

data class TransactionOutput(
    val amount: BigInteger,
    val scriptPubKey: Script
) {
    override fun toString() = "$amount:$scriptPubKey"

    companion object {
        fun parse(stream: ByteArrayInputStream) = TransactionOutput(
            amount = stream.readNBytes(8).littleEndianToBigInteger(),
            scriptPubKey = Script.parse(stream)
        )
    }

    fun serialize() = amount.toLittleEndianByteArray().copyOf(8) + scriptPubKey.serialize()

}