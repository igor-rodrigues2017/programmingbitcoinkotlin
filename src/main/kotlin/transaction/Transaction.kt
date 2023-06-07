package transaction

import extension.*
import script.Script
import script.p2pkhScriptSig
import signature.PrivateKey
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.math.BigInteger.ZERO

data class Transaction(
    val version: BigInteger,
    val inputs: List<TransactionInput> = listOf(),
    val outputs: List<TransactionOutput> = listOf(),
    val lockTime: BigInteger = ZERO,
    val testnet: Boolean = false
) {

    companion object {
        fun parse(stream: ByteArrayInputStream, testnet: Boolean = false) = Transaction(
            version = stream.readNBytes(4).littleEndianToBigInteger(),
            inputs = parseInputs(stream),
            outputs = parseOutPuts(stream),
            lockTime = stream.readNBytes(4).littleEndianToBigInteger(),
            testnet = testnet
        )

        private fun parseInputs(stream: ByteArrayInputStream): MutableList<TransactionInput> {
            val numInputs = stream.readVarInt()
            return mutableListOf<TransactionInput>().let { inputs ->
                repeat(numInputs.toInt()) {
                    inputs += TransactionInput.parse(stream)
                }
                inputs
            }
        }

        private fun parseOutPuts(stream: ByteArrayInputStream): List<TransactionOutput> {
            val numOutputs = stream.readVarInt()
            return mutableListOf<TransactionOutput>().let { outputs ->
                repeat(numOutputs.toInt()) {
                    outputs += TransactionOutput.parse(stream)
                }
                outputs
            }
        }
    }

    fun id() = hash().toHex()

    fun fee(): BigInteger = inputs.sumOf { it.value(this.testnet) } - outputs.sumOf { it.amount }

    fun serialize() = serializeVersion() + serializeInputs() + serializeOutputs() + serializeLockTime()

    fun sigHash(inputIndex: Int, redeemScript: Script? = null): BigInteger {
        return hash256(
            serializeVersion() +
                    serializeInputsForSigHash(inputIndex, redeemScript) +
                    serializeOutputs() +
                    serializeLockTime() +
                    serializeHashType()
        ).toBigInteger()
    }

    fun signInput(index: Int, privateKey: PrivateKey): Boolean = sigHash(index).let { z ->
        setScriptSigToInput(getScriptSig(privateKey, z), index)
        verifyInput(index)
    }

    private fun setScriptSigToInput(scriptSig: Script, index: Int) {
        inputs[index].scriptSignature = scriptSig
    }

    private fun getScriptSig(privateKey: PrivateKey, z: BigInteger) =
        p2pkhScriptSig(
            derSignature = signInput(privateKey, z),
            secPublicKey = privateKey.publicKey.sec()
        )

    private fun signInput(privateKey: PrivateKey, z: BigInteger): ByteArray {
        val hashType = SIGHASH_ALL.toByteArray()
        return privateKey.sign(z).der() + hashType
    }

    fun verifyInput(inputIndex: Int): Boolean {
        return inputs[inputIndex].let { input ->
            val scriptPubKey = input.scriptPubkey(testnet)
            val z = sigHash(inputIndex, getRedeemScript(scriptPubKey, input))
            combineSignatureAndPubkeyScripts(input).evaluate(z)
        }
    }

    private fun getRedeemScript(scriptPubkey: Script, input: TransactionInput) =
        if (scriptPubkey.isP2shScriptPubkey()) {
            val command = input.scriptSignature.commands.last() as ByteArray
            val rawRedeem = command.size.toBigInteger().toVarInt().let { it + command }
            Script.parse(rawRedeem.inputStream())
        } else null

    /**
     * Note that a full node would verify more things, like checking for double-spends and
     * checking some other consensus rules
     */
    fun verify(): Boolean {
        if (creatingMoney()) return false
        inputs.forEachIndexed { index, _ ->
            if (isInvalidInput(index)) return false
        }
        return true
    }

    private fun combineSignatureAndPubkeyScripts(input: TransactionInput) =
        input.scriptSignature + input.scriptPubkey(testnet)

    private fun isInvalidInput(index: Int) = !verifyInput(index)

    private fun creatingMoney() = fee() < ZERO

    /**
     * Serialization in little-endian
     */
    private fun hash() = hash256(serialize()).reversedArray()

    private fun serializeInputsForSigHash(inputIndex: Int, redeemScript: Script? = null): ByteArray {
        return lengthInVarInt(inputs) + modifySignatureInInputs(inputIndex, redeemScript)
    }

    private fun modifySignatureInInputs(inputIndex: Int, redeemScript: Script? = null) =
        inputs.foldIndexed(byteArrayOf()) { index, acc, transactionInput ->
            acc + newScriptSignature(index, inputIndex, transactionInput, redeemScript)
        }

    private fun newScriptSignature(
        index: Int,
        inputIndex: Int,
        transactionInput: TransactionInput,
        redeemScript: Script? = null
    ) = if (index == inputIndex) {
        redeemScript?.let { script ->
            exchangeScriptSignatureToRedeemScript(transactionInput, script)
        } ?: exchangeScriptSignatureToScriptPubkey(transactionInput)
    } else {
        emptyScriptSignature(transactionInput)
    }

    private fun exchangeScriptSignatureToRedeemScript(
        transactionInput: TransactionInput,
        redeemScript: Script
    ) = transactionInput.copy(
        scriptSignature = redeemScript
    ).serialize()

    private fun exchangeScriptSignatureToScriptPubkey(transactionInput: TransactionInput) = transactionInput.copy(
        scriptSignature = transactionInput.scriptPubkey(testnet),
    ).serialize()

    private fun emptyScriptSignature(transactionInput: TransactionInput) = transactionInput.copy(
        scriptSignature = Script(),
    ).serialize()

    private fun serializeVersion() = version.toLittleEndianByteArray().copyOf(4)

    private fun serializeInputs() = lengthInVarInt(inputs) +
            inputs.fold(byteArrayOf()) { acc, transaction -> acc + transaction.serialize() }

    private fun serializeOutputs() = lengthInVarInt(outputs) +
            outputs.fold(byteArrayOf()) { acc, transaction -> acc + transaction.serialize() }

    private fun serializeLockTime() = lockTime.toLittleEndianByteArray().copyOf(4)

    private fun lengthInVarInt(transactionInputs: List<Any>) = transactionInputs.size.toBigInteger().toVarInt()

    private fun serializeHashType() = SIGHASH_ALL.toLittleEndianByteArray().copyOf(4)

    override fun toString() = """
        |transaction: ${id()}
        |version: $version
        |inputs: 
        |${inputs.joinToString("\n").indent(4)}
        |outputs: 
        |${outputs.joinToString("\n").indent(4)}
        |locktime: $lockTime
    """.trimMargin()
}

