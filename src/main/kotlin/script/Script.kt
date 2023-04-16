package script

import extension.littleEndianToBigInteger
import extension.readVarint
import extension.toHex
import extension.toLittleEndianByteArray
import extension.toVarint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KFunction

private const val OP_DUP = 0x76
private const val OP_HASH160 = 0xa9
private const val OP_EQUALVERIFY = 0x88
private const val OPCHECKSIG = 0xac

fun p2pkhScriptPubkey(h160Pubkey: ByteArray) = Script(listOf(OP_DUP, OP_HASH160, h160Pubkey, OP_EQUALVERIFY, OPCHECKSIG))

fun p2pkhScriptSig(derSignature: ByteArray, secPublicKey: ByteArray) = Script(listOf(derSignature, secPublicKey))

class Script(val commands: List<Any> = listOf()) {

    companion object {
        fun parse(stream: ByteArrayInputStream): Script = Stack<Any>().let { commands ->
            val scriptLength = getLength(stream)
            var count = 0
            while (count < scriptLength) {
                val current = stream.read()
                count += 1
                val currentByte = current.toByte()
                when {
                    isElement(currentByte) -> {
                        count = readBytesAndCount(currentByte, commands, stream, count)
                    }

                    isPushData1(currentByte) -> {
                        count = readPushData1AndCount(stream, commands, count)
                    }

                    isPushData2(currentByte) -> {
                        count = readPushData2AndCount(stream, commands, count)
                    }

                    else -> {
                        addOpCode(commands, current)
                    }
                }
            }
            validateParseScript(count, scriptLength)
            return Script(commands)
        }

        private fun getLength(stream: ByteArrayInputStream) = stream.readVarint().toInt()

        private fun isElement(currentByte: Byte) = currentByte >= 1.toByte() && currentByte <= 75.toByte()

        private fun isPushData1(currentByte: Byte) = currentByte == 76.toByte()

        private fun isPushData2(currentByte: Byte) = currentByte == 77.toByte()

        private fun readPushData1AndCount(
            stream: ByteArrayInputStream,
            commands: Stack<Any>,
            count: Int
        ): Int {
            var count1 = count
            val dataLength = stream.readNBytes(1).littleEndianToBigInteger().toInt()
            commands.add(stream.readNBytes(dataLength))
            count1 += dataLength + 1
            return count1
        }

        private fun readPushData2AndCount(
            stream: ByteArrayInputStream,
            commands: Stack<Any>,
            count: Int
        ): Int {
            var count1 = count
            val dataLength = stream.readNBytes(2).littleEndianToBigInteger().toInt()
            commands.add(stream.readNBytes(dataLength))
            count1 += dataLength + 2
            return count1
        }

        private fun addOpCode(commands: Stack<Any>, opCode: Int) {
            commands.add(opCode)
        }

        private fun readBytesAndCount(
            currentByte: Byte,
            commands: Stack<Any>,
            stream: ByteArrayInputStream,
            count: Int
        ): Int {
            var count1 = count
            val nextNBytes = currentByte.toInt()
            commands.add(stream.readNBytes(nextNBytes))
            count1 += nextNBytes
            return count1
        }

        private fun validateParseScript(count: Int, scriptLength: Int) {
            if (count != scriptLength) {
                throw IllegalArgumentException("parsing script failed")
            }
        }
    }

    operator fun plus(other: Script): Script = Stack<Any>().let {
        it.addAll(this.commands)
        it.addAll(other.commands)
        Script(it)
    }

    fun serialize(): ByteArray {
        val result = rawSerialize()
        val length = result.size.toBigInteger().toVarint()
        return length + result
    }

    private fun rawSerialize(): ByteArray {
        val result = ByteArrayOutputStream()
        for (command in commands) {
            when (command) {
                is Int -> result.write(toBytes(command, 1))
                else -> {
                    val length = (command as ByteArray).size
                    when {
                        isElement(length) -> {
                            result.write(toBytes(length, 1))
                        }

                        isPushData1(length) -> {
                            result.write(toBytes(76, 1))
                            result.write(toBytes(length, 1))
                        }

                        isPushData2(length) -> {
                            result.write(toBytes(77, 1))
                            result.write(toBytes(length, 2))
                        }

                        else -> throw IllegalArgumentException("too long an command")
                    }
                    result.write(command)
                }
            }
        }
        return result.toByteArray()
    }

    private fun toBytes(length: Int, arraySize: Int) = length.toBigInteger().toLittleEndianByteArray().copyOf(arraySize)

    private fun isElement(length: Int) = length < 75

    private fun isPushData1(length: Int) = length in 76..255

    private fun isPushData2(length: Int) = length in 256..520
    fun evaluate(z: BigInteger): Boolean {
        val commandsCopy = commands.toMutableList()
        val stack = Stack<Any>()
        val altStack = Stack<Any>()
        while (commandsCopy.isNotEmpty()) {
            val command = commandsCopy.removeFirst()
            if (command is Int) {
                executeOperation(command, stack, commandsCopy, altStack, z).let { operation ->
                    if (isFail(operation)) return false
                }
            } else stack.add(command)
        }
        return isScriptSuccess(stack)
    }

    private fun executeOperation(
        command: Int,
        stack: Stack<Any>,
        commandsCopy: List<Any>,
        altStack: Stack<Any>,
        z: BigInteger
    ): Boolean {
        val operation = OP_CODE_FUNCTIONS[command]!!
        when (command) {
            in listOf(99, 100) -> {
                executeOperationIf(operation, stack, commandsCopy, command).let { result ->
                    if (isFail(result)) return false
                }
            }

            in listOf(107, 108) -> {
                executeOperationAltStack(operation, stack, altStack, command).let { result ->
                    if (isFail(result)) return false
                }
            }

            in listOf(172, 173, 174, 175) -> {
                executeOperationCheck(operation, stack, z, command).let { result ->
                    if (isFail(result)) return false
                }
            }

            else -> {
                executeOperationStack(operation, stack, command).let { result ->
                    if (isFail(result)) return false
                }
            }
        }
        return true
    }

    private fun executeOperationStack(
        operation: KFunction<Boolean>,
        stack: Stack<Any>,
        command: Any?
    ) = runCatching { operation.call(stack) }.onFailure {
        Logger.getAnonymousLogger().log(Level.INFO, it.message)
        Logger.getAnonymousLogger().log(Level.INFO, "bad op: ${OP_CODE_NAMES[command]}")
    }.getOrDefault(false)

    private fun executeOperationCheck(
        operation: KFunction<Boolean>,
        stack: Stack<Any>,
        z: BigInteger,
        command: Any?
    ) = runCatching { operation.call(stack, z) }.onFailure {
        logError(command, it)
    }.getOrDefault(false)

    private fun executeOperationAltStack(
        operation: KFunction<Boolean>,
        stack: Stack<Any>,
        altStack: Stack<Any>,
        command: Any?
    ) = runCatching { operation.call(stack, altStack) }.onFailure {
        logError(command, it)
    }.getOrDefault(false)

    private fun executeOperationIf(
        operation: KFunction<Boolean>,
        stack: Stack<Any>,
        commandsCopy: List<Any>,
        command: Any?
    ): Boolean {
        return runCatching { operation.call(stack, commandsCopy) }.onFailure {
            logError(command, it)
        }.getOrDefault(false)
    }

    private fun isFail(resultOperation: Boolean) = !resultOperation

    private fun logError(command: Any?, exception: Throwable) {
        Logger.getAnonymousLogger().log(Level.INFO, "bad op: ${OP_CODE_NAMES[command]}")
        Logger.getAnonymousLogger().log(Level.INFO, exception.message)
        throw exception
    }

    private fun isScriptSuccess(stack: Stack<Any>) = stack.size != 0 || stack.pop() != byteArrayOf()

    override fun toString(): String {
        val result = mutableListOf<String>()
        commands.forEach { command ->
            when(command) {
                is Int -> {
                    val name = if (OP_CODE_NAMES.containsKey(command)) OP_CODE_NAMES[command] else "OP_[$command]"
                    result.add(name!!)
                }
                else -> result.add((command as ByteArray).toHex())
            }
        }

        return result.joinToString(" ")
    }

}