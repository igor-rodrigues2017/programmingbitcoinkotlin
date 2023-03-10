package script

import extension.littleEndianToBigInteger
import extension.readVarint
import extension.toLittleEndianByteArray
import extension.toVarint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Script(
    val commands: List<Any> = listOf()
) {

    companion object {
        fun parse(stream: ByteArrayInputStream): Script {
            val length = stream.readVarint()
            val commands = mutableListOf<Any>()
            var count = 0
            while (count.toBigInteger() < length) {
                val current = stream.read()
                count += 1
                val currentByte = current.toByte()
                when {
                    currentByte >= 1.toByte() && currentByte <= 75.toByte() -> {
                        val n = currentByte.toInt()
                        commands.add(stream.readNBytes(n))
                        count += n
                    }
                    currentByte == 76.toByte() -> {
                        val dataLength = stream.readNBytes(1).littleEndianToBigInteger().toInt()
                        commands.add(stream.readNBytes(dataLength))
                        count += dataLength + 1
                    }
                    currentByte == 77.toByte() -> {
                        val dataLength = stream.readNBytes(2).littleEndianToBigInteger().toInt()
                        commands.add(stream.readNBytes(dataLength))
                        count += dataLength + 2
                    }
                    else -> {
                        commands.add(current)
                    }
                }
            }
            if (count != length.toInt()) {
                throw IllegalArgumentException("parsing script failed")
            }
            return Script(commands)
        }
    }

    fun serialize(): ByteArray {
        val result = rawSerialize()
        val total = result.size.toBigInteger()
        return total.toVarint() + result
    }

    private fun rawSerialize(): ByteArray {
        val result = ByteArrayOutputStream()
        for (command in commands) {
            when (command) {
                is Int -> result.write(command.toBigInteger().toLittleEndianByteArray().copyOf(1))
                else -> {
                    val length = (command as ByteArray).size
                    when {
                        length < 75 -> {
                            result.write(length.toBigInteger().toLittleEndianByteArray().copyOf(1))
                        }

                        length in 76..255 -> {
                            result.write(76.toBigInteger().toLittleEndianByteArray().copyOf(1))
                            result.write(length.toBigInteger().toLittleEndianByteArray().copyOf(1))
                        }

                        length in 256..520 -> {
                            result.write(77.toBigInteger().toLittleEndianByteArray().copyOf(1))
                            result.write(length.toBigInteger().toLittleEndianByteArray().copyOf(2))
                        }

                        else -> throw IllegalArgumentException("too long an command")
                    }
                    result.write(command)
                }
            }
        }
        return result.toByteArray()
    }

}
