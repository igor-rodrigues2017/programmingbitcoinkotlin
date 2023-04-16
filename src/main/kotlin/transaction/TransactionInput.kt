package transaction

import extension.littleEndianToBigInteger
import extension.toHex
import extension.toLittleEndianByteArray
import script.Script
import java.io.ByteArrayInputStream
import java.math.BigInteger

data class TransactionInput(
    val previousTransactionId: ByteArray,
    val previousIndex: BigInteger,
    var scriptSignature: Script = Script(),
    val sequence: BigInteger = 0xffffffff.toBigInteger()
) {

    companion object {
        fun parse(stream: ByteArrayInputStream) = TransactionInput(
            previousTransactionId = stream.readNBytes(32).reversedArray(),
            previousIndex = stream.readNBytes(4).littleEndianToBigInteger(),
            scriptSignature = Script.parse(stream),
            sequence = stream.readNBytes(4).littleEndianToBigInteger()
        )
    }

    fun serialize(): ByteArray = previousTransactionId.reversedArray() +
            previousIndex.toLittleEndianByteArray().copyOf(4) +
            scriptSignature.serialize() +
            sequence.toLittleEndianByteArray().copyOf(4)

    fun value(testnet: Boolean = false) = outputOrigin(testnet).amount

    fun scriptPubkey(testnet: Boolean = false) = outputOrigin(testnet).scriptPubKey

    private fun outputOrigin(testnet: Boolean) = fetchPrevious(testnet).outputs[previousIndex.toInt()]

    private fun fetchPrevious(testnet: Boolean = false) = TransactionFetcher.fetch(previousTransactionId.toHex(), testnet)
    override fun toString() = "${previousTransactionId.toHex()}:$previousIndex"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionInput

        if (!previousTransactionId.contentEquals(other.previousTransactionId)) return false
        if (previousIndex != other.previousIndex) return false
        if (scriptSignature != other.scriptSignature) return false
        if (sequence != other.sequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = previousTransactionId.contentHashCode()
        result = 31 * result + previousIndex.hashCode()
        result = 31 * result + scriptSignature.hashCode()
        result = 31 * result + sequence.hashCode()
        return result
    }

}