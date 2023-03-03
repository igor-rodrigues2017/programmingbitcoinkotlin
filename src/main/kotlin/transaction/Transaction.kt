package transaction

import extension.*
import java.io.ByteArrayInputStream
import java.math.BigInteger

data class Transaction(
    val version: BigInteger,
    val inputs: List<TransactionInput> = listOf(),
    val outputs: List<TransactionOutput> = listOf(),
    val lockTime: BigInteger = BigInteger.ZERO,
    val testnet: Boolean = false
) {

    companion object {
        fun parse(stream: ByteArrayInputStream, testnet: Boolean = false) = Transaction(
            version = stream.readNBytes(4).littleEndianToBigInteger(),
            inputs = parseInputs(stream),
            outputs = paseOutPuts(stream),
            lockTime = stream.readNBytes(4).littleEndianToBigInteger(),
            testnet = testnet
        )

        private fun parseInputs(stream: ByteArrayInputStream): MutableList<TransactionInput> {
            val numInputs = stream.readVarint()
            return mutableListOf<TransactionInput>().let { inputs ->
                repeat(numInputs.toInt()) {
                    inputs += TransactionInput.parse(stream)
                }
                inputs
            }
        }

        private fun paseOutPuts(stream: ByteArrayInputStream): List<TransactionOutput> {
            val numOutputs = stream.readVarint()
            return mutableListOf<TransactionOutput>().let { outputs ->
                repeat(numOutputs.toInt()) {
                    outputs += TransactionOutput.parse(stream)
                }
                outputs
            }
        }
    }

    fun id() = hash().toHex()

    /**
     * Serialization in little-endian
     */
    private fun hash() = hash256(serialize()).reversedArray()

    fun serialize() = serializeVersion() + serializeInputs() + serializeOutputs() + serializeLockTime()

    private fun serializeVersion() = version.toLittleEndianByteArray().copyOf(4)

    private fun serializeInputs() = lengthInVarint(inputs) +
            inputs.fold(byteArrayOf()) { acc, transaction -> acc + transaction.serialize() }

    private fun serializeOutputs() = lengthInVarint(outputs) +
            outputs.fold(byteArrayOf()) { acc, transaction -> acc + transaction.serialize() }

    private fun serializeLockTime() = lockTime.toLittleEndianByteArray().copyOf(4)

    private fun lengthInVarint(transactionInputs: List<Any>) = transactionInputs.size.toBigInteger().toVarint()

    override fun toString() = """
        transaction: ${id()}
        version: $version
        inputs: ${inputs.joinToString("\n")}
        outputs: ${outputs.joinToString("\n")}
        locktime: $lockTime
    """.trimIndent()

    fun fee(): BigInteger = inputs.sumOf { it.value() } - outputs.sumOf { it.amount }
}

