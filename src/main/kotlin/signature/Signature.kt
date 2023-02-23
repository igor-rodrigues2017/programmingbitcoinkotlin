package signature

import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.util.*

private const val MARKER_02: Byte = 0x02
private const val MARKER_30: Byte = 0x30
data class Signature(val r: BigInteger, val s: BigInteger) {

    companion object {
        fun parse(signatureBin: ByteArray): Signature = ByteArrayInputStream(signatureBin).let {
            verifyMarker(it.read().toByte(), MARKER_30)
            verifyLength(it.read().toByte(), signatureBin)
            verifyMarker(it.read().toByte(), MARKER_02)
            val rLength = it.read().toByte()
            val r = BigInteger(1, it.readNBytes(rLength.toInt()))
            verifyMarker(it.read().toByte(), MARKER_02)
            val sLength = it.read().toByte()
            val s = BigInteger(1, it.readNBytes(sLength.toInt()))
            verifySignatureLength(signatureBin, rLength, sLength)
            return Signature(r, s)
        }

        private fun verifyMarker(byte: Byte, marker: Byte) {
            if (byte != marker) {
                throw InvalidPropertiesFormatException("Bad Signature")
            }
        }

        private fun verifyLength(length: Byte, signatureBin: ByteArray) {
            if ((length + 2).toByte() != signatureBin.size.toByte()) {
                throw InvalidPropertiesFormatException("Bad Signature Length")
            }
        }

        private fun verifySignatureLength(signatureBin: ByteArray, rLength: Byte, sLength: Byte) {
            if (signatureBin.size != 6 + rLength.toInt() + sLength.toInt()) {
                throw InvalidPropertiesFormatException("Signature too long")
            }
        }
    }
    fun dre(): ByteArray = serialize(prepareByteArray(r), prepareByteArray(s)).let { putMarkerAndLength(MARKER_30, it) }
    private fun prepareByteArray(bigInteger: BigInteger) = bytesWithoutNullBytesAtBeginning(bigInteger).let {
            if (checkMostSignificantBit(it)) byteArrayOf(0x00) + it else it
        }

    /**
     * The first byte starting with something greater than or
     * equal to 0x80 are because DER is a general encoding and allows for negative numbers
     * to be encoded. The first bit being 1 means that the number is negative. All numbers
     * in an ECDSA signature are positive, so we have to prepend with 0x00 if the first bit is
     * zero, which is equivalent to first byte ≥ 0x80.
     */
    private fun checkMostSignificantBit(bytes: ByteArray) = bytes[0].toInt() and 0x80 != 0

    private fun bytesWithoutNullBytesAtBeginning(bigInteger: BigInteger) =
        bigInteger.toByteArray().dropWhile { it == 0.toByte() }.toByteArray()

    private fun serialize(
        rBytes: ByteArray,
        sBytes: ByteArray
    ) = putMarkerAndLength(MARKER_02, rBytes) + putMarkerAndLength(MARKER_02, sBytes)

    private fun putMarkerAndLength(
        marker: Byte,
        bytes: ByteArray
    ) = byteArrayOf(marker, bytes.size.toByte()) + bytes

    override fun toString() = "Signature(r=${r.toString(16)}, s=${s.toString(16)})"
}