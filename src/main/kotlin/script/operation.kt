package script

import ellipticcurve.S256Point
import extension.*
import signature.Signature
import java.math.BigInteger
import java.math.BigInteger.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

fun encodeNum(num: BigInteger): ByteArray {
    if (num == ZERO) return ByteArray(0)
    return num.toByteArray().reversedArray()
}

fun decodeNum(element: ByteArray): BigInteger {
    if (element.isEmpty()) return ZERO
    return element.reversedArray().let { bigEndian -> BigInteger(bigEndian) }
}

fun op0(stack: Stack<ByteArray>) = stack.add(encodeNum(ZERO))

fun op1Negate(stack: Stack<ByteArray>) = stack.add(encodeNum(-ONE))

fun op1(stack: Stack<ByteArray>) = stack.add(encodeNum(ONE))

fun op2(stack: Stack<ByteArray>) = stack.add(encodeNum(TWO))

fun op3(stack: Stack<ByteArray>) = stack.add(encodeNum(3.toBigInteger()))

fun op4(stack: Stack<ByteArray>) = stack.add(encodeNum(4.toBigInteger()))

fun op5(stack: Stack<ByteArray>) = stack.add(encodeNum(5.toBigInteger()))

fun op6(stack: Stack<ByteArray>) = stack.add(encodeNum(6.toBigInteger()))

fun op7(stack: Stack<ByteArray>) = stack.add(encodeNum(7.toBigInteger()))

fun op8(stack: Stack<ByteArray>) = stack.add(encodeNum(8.toBigInteger()))

fun op9(stack: Stack<ByteArray>) = stack.add(encodeNum(9.toBigInteger()))

fun op10(stack: Stack<ByteArray>) = stack.add(encodeNum(TEN))

fun op11(stack: Stack<ByteArray>) = stack.add(encodeNum(11.toBigInteger()))

fun op12(stack: Stack<ByteArray>) = stack.add(encodeNum(12.toBigInteger()))

fun op13(stack: Stack<ByteArray>) = stack.add(encodeNum(13.toBigInteger()))

fun op14(stack: Stack<ByteArray>) = stack.add(encodeNum(14.toBigInteger()))

fun op15(stack: Stack<ByteArray>) = stack.add(encodeNum(15.toBigInteger()))

fun op16(stack: Stack<ByteArray>) = stack.add(encodeNum(16.toBigInteger()))

fun opNop(stack: Stack<ByteArray>) = true

fun opIf(stack: Stack<ByteArray>, items: MutableList<Any>): Boolean {
    if (stack.size < 1) return false
    val trueItems = mutableListOf<Any>()
    val falseItems = mutableListOf<Any>()
    var currentArray = trueItems
    var found = false
    var numEndIfsNeeded = 1
    while (items.isNotEmpty()) {
        when (val item = items.removeFirst()) {
            99, 100 -> {
                // Nested if, we have to go another endif
                numEndIfsNeeded++
                currentArray.add(item)
            }

            103 -> {
                if (numEndIfsNeeded == 1) {
                    currentArray = falseItems
                } else {
                    currentArray.add(item)
                }
            }

            104 -> {
                if (numEndIfsNeeded == 1) {
                    found = true
                    break
                } else {
                    numEndIfsNeeded--
                    currentArray.add(item)
                }
            }

            else -> currentArray.add(item)
        }
    }
    if (!found) return false
    val element = stack.pop()
    if (decodeNum(element) == ZERO) {
        items.addAll(0, falseItems)
    } else {
        items.addAll(0, trueItems)
    }
    return true
}

fun opNotIf(stack: Stack<ByteArray>, items: Stack<Any>): Boolean {
    if (stack.size < 1) return false
    // go through and re-make the items array based on the top stack element
    val trueItems = mutableListOf<Any>()
    val falseItems = mutableListOf<Any>()
    var currentArray = trueItems
    var found = false
    var numEndIfsNeeded = 1
    while (items.isNotEmpty()) {
        val item = items.removeAt(0)
        if (item == 99 || item == 100) {
            // nested if, we have to go another endif
            numEndIfsNeeded++
            currentArray.add(item)
        } else if (numEndIfsNeeded == 1 && item == 103) {
            currentArray = falseItems
        } else if (item == 104) {
            if (numEndIfsNeeded == 1) {
                found = true
                break
            } else {
                numEndIfsNeeded--
                currentArray.add(item)
            }
        } else {
            currentArray.add(item)
        }
    }
    if (!found) return false
    val element = stack.pop()
    if (decodeNum(element) == ZERO) {
        items.addAll(0, trueItems)
    } else {
        items.addAll(0, falseItems)
    }
    return true
}

fun opVerify(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return decodeNum(element) != ZERO
}

fun opToAltStack(stack: Stack<ByteArray>, altStack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    return altStack.add(stack.pop())
}

fun opReturn(stack: Stack<ByteArray>) = false

fun opFromAltStack(stack: Stack<ByteArray>, altStack: Stack<ByteArray>): Boolean {
    if (altStack.size < 1) return false
    return stack.add(altStack.pop())
}

fun op2drop(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    repeat(2) { stack.pop() }
    return true
}

fun op2dup(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    stack.addAll(stack.takeLast(2))
    return true
}

fun op3dup(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 3) return false
    stack.addAll(stack.takeLast(3))
    return true
}

fun op2over(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 4) return false
    return stack.addAll(stack.negativeRangeIndex(-4, -2))
}

fun op2rot(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 6) return false
    return stack.addAll(stack.negativeRangeIndex(-6, -4))
}

fun op2swap(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 4) return false
    val last2 = stack.takeLast(2).toList()
    val middle = stack.negativeRangeIndex(-4, -2)
    stack.subList(stack.size - 4, stack.size).clear()
    stack.addAll(last2)
    stack.addAll(middle)
    return true
}

fun opIfDup(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    if (decodeNum(stack.last()) != ZERO) stack.add(stack.last())
    return true
}

fun opDepth(stack: Stack<ByteArray>): Boolean {
    return stack.add(encodeNum(stack.size.toBigInteger()))
}

fun opDrop(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    stack.pop()
    return true
}

fun opDup(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    return stack.add(stack.last())
}

fun opNip(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val last = stack.last()
    stack.subList(stack.size - 2, stack.size).clear()
    return stack.add(last)
}

fun opOver(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    return stack.add(stack.negativeIndex(-2))
}

fun opPick(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val n = decodeNum(stack.pop()).toInt()
    if (stack.size < n + 1) return false
    return stack.add(stack.negativeIndex(-n - 1))
}

fun opRoll(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val n = decodeNum(stack.pop()).toInt()
    if (stack.size < n + 1) return false
    if (stack.size == 0) return true
    return stack.add(stack.removeNegativeIndex(-n - 1))
}

fun opRot(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 3) return false
    val element = stack.negativeIndex(-3)
    stack.removeNegativeIndex(-3)
    return stack.add(element)
}

fun opSwap(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    return stack.add(stack.removeNegativeIndex(-2))
}

fun opTuck(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    stack.insertElementAt(stack.last(), stack.size - 2)
    return true
}

fun opSize(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    return stack.add(encodeNum(stack.last().size.toBigInteger()))
}

fun opEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = stack.pop()
    val element2 = stack.pop()
    return if (element1.contentEquals(element2)) stack.add(encodeNum(1.toBigInteger()))
    else stack.add(encodeNum(ZERO))
}

fun opEqualVerify(stack: Stack<ByteArray>): Boolean {
    return opEqual(stack) and opVerify(stack)
}

fun op1add(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = decodeNum(stack.pop())
    return stack.add(encodeNum(element + ONE))
}

fun op1sub(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = decodeNum(stack.pop())
    return stack.add(encodeNum(element - ONE))
}

fun opNegate(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = decodeNum(stack.pop())
    return stack.add(encodeNum(-element))
}

fun opAbs(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = decodeNum(stack.pop())
    return if (element < ZERO) stack.add(encodeNum(-element))
    else stack.add(encodeNum(element))
}

fun opNot(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return if (decodeNum(element) == ZERO) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun op0NotEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return if (decodeNum(element) == ZERO) stack.add(encodeNum(ZERO))
    else stack.add(encodeNum(ONE))
}

fun opAdd(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return stack.add(encodeNum(element1 + element2))
}

fun opSub(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return stack.add(encodeNum(element1 - element2))
}

fun opMul(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return stack.add(encodeNum(element2 * element1))
}

fun opBoolAnd(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 != ZERO && element2 != ZERO) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opBoolOr(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 != ZERO || element2 != ZERO) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opNumEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 == element2) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opNumEqualVerify(stack: Stack<ByteArray>): Boolean {
    return opNumEqual(stack) && opVerify(stack)
}

fun opNumNotEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 == element2) stack.add(encodeNum(ZERO))
    else stack.add(encodeNum(ONE))
}

fun opLessThan(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element2 < element1) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opGreaterThan(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element2 > element1) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opLessThanOrEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element2 <= element1) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opGreaterThanOrEqual(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element2 >= element1) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opMin(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 < element2) stack.add(encodeNum(element1))
    else stack.add(encodeNum(element2))
}

fun opMax(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 2) return false
    val element1 = decodeNum(stack.pop())
    val element2 = decodeNum(stack.pop())
    return if (element1 > element2) stack.add(encodeNum(element1))
    else stack.add(encodeNum(element2))
}

fun opWithin(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 3) return false
    val maximum = decodeNum(stack.pop())
    val minimum = decodeNum(stack.pop())
    val element = decodeNum(stack.pop())
    return if (element >= minimum && element < maximum) stack.add(encodeNum(ONE))
    else stack.add(encodeNum(ZERO))
}

fun opRipemd160(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return stack.add(hash160(element))
}

fun opSha1(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return stack.add(sha1(element))
}

fun opSha256(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return stack.add(sha256(element))
}

fun opHash160(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return stack.add(hash160(element))
}

fun opHash256(stack: Stack<ByteArray>): Boolean {
    if (stack.size < 1) return false
    val element = stack.pop()
    return stack.add(hash256(element))
}

fun opCheckSig(stack: Stack<ByteArray>, z: BigInteger): Boolean {
    if (stack.size < 2) return false
    val secPubKey = stack.pop()
    val dreSignature = getDreSignatureWithoutHashType(stack)
    runCatching {
        val signature = Signature.parse(dreSignature)
        val pubKey = S256Point.parse(secPubKey)
        if (pubKey.verify(z, signature)) stack.add(encodeNum(ONE)) else stack.add(byteArrayOf())
    }.onFailure {
        Logger.getAnonymousLogger().log(Level.INFO, it.message)
        return false
    }
    return true
}

/**
 * take off the last byte of the signature as that's the hash_type
 */
private fun getDreSignatureWithoutHashType(stack: Stack<ByteArray>) = stack.pop().dropLast(1).toByteArray()

fun opCheckSigVerify(stack: Stack<ByteArray>, z: BigInteger) = opCheckSig(stack, z) && opVerify(stack)

fun opCheckMultiSig(stack: Stack<ByteArray>, z: BigInteger): Boolean {
    TODO("NOT IMPLEMENTED")
}

fun opCheckMultiSigVerify(stack: Stack<ByteArray>, z: BigInteger) = opCheckMultiSig(stack, z) && opVerify(stack)

fun opCheckLockTimeVerify(
    stack: Stack<ByteArray>,
    lockTime: BigInteger,
    sequence: BigInteger
): Boolean {
    if (sequence == 0xffffffff.toBigInteger()) return false
    if (stack.size < 1) return false
    val element = decodeNum(stack.last())
    if (element < ZERO) return false
    if (element < 500_000_000.toBigInteger() &&
        lockTime > 500_000_000.toBigInteger()
    ) return false
    if (lockTime < element) return false
    return true
}

fun opCheckSequenceVerify(
    stack: Stack<ByteArray>,
    version: Int,
    sequence: BigInteger
): Boolean {
    if (sequence and (1 shl 31).toBigInteger() == (1 shl 31).toBigInteger()) {
        return false
    }
    if (stack.size < 1) {
        return false
    }
    val element = decodeNum(stack.last())
    if (element < ZERO) {
        return false
    }
    if (element.testBit(31)) {
        if (version < 2) {
            return false
        } else if (sequence.testBit(31)) {
            return false
        } else if (element and ((1 shl 22) - 1).toBigInteger() != sequence and ((1 shl 22) - 1).toBigInteger()) {
            return false
        } else if ((element and 0xffff.toBigInteger()) > (sequence and 0xffff.toBigInteger())) {
            return false
        }
    }
    return true
}

val OP_CODE_NAMES = mapOf(
    0 to "OP_0",
    76 to "OP_PUSHDATA1",
    77 to "OP_PUSHDATA2",
    78 to "OP_PUSHDATA4",
    79 to "OP_1NEGATE",
    81 to "OP_1",
    82 to "OP_2",
    83 to "OP_3",
    84 to "OP_4",
    85 to "OP_5",
    86 to "OP_6",
    87 to "OP_7",
    88 to "OP_8",
    89 to "OP_9",
    90 to "OP_10",
    91 to "OP_11",
    92 to "OP_12",
    93 to "OP_13",
    94 to "OP_14",
    95 to "OP_15",
    96 to "OP_16",
    97 to "OP_NOP",
    99 to "OP_IF",
    100 to "OP_NOTIF",
    103 to "OP_ELSE",
    104 to "OP_ENDIF",
    105 to "OP_VERIFY",
    106 to "OP_RETURN",
    107 to "OP_TOALTSTACK",
    108 to "OP_FROMALTSTACK",
    109 to "OP_2DROP",
    110 to "OP_2DUP",
    111 to "OP_3DUP",
    112 to "OP_2OVER",
    113 to "OP_2ROT",
    114 to "OP_2SWAP",
    115 to "OP_IFDUP",
    116 to "OP_DEPTH",
    117 to "OP_DROP",
    118 to "OP_DUP",
    119 to "OP_NIP",
    120 to "OP_OVER",
    121 to "OP_PICK",
    122 to "OP_ROLL",
    123 to "OP_ROT",
    124 to "OP_SWAP",
    125 to "OP_TUCK",
    130 to "OP_SIZE",
    135 to "OP_EQUAL",
    136 to "OP_EQUALVERIFY",
    139 to "OP_1ADD",
    140 to "OP_1SUB",
    143 to "OP_NEGATE",
    144 to "OP_ABS",
    145 to "OP_NOT",
    146 to "OP_0NOTEQUAL",
    147 to "OP_ADD",
    148 to "OP_SUB",
    149 to "OP_MUL",
    154 to "OP_BOOLAND",
    155 to "OP_BOOLOR",
    156 to "OP_NUMEQUAL",
    157 to "OP_NUMEQUALVERIFY",
    158 to "OP_NUMNOTEQUAL",
    159 to "OP_LESSTHAN",
    160 to "OP_GREATERTHAN",
    161 to "OP_LESSTHANOREQUAL",
    162 to "OP_GREATERTHANOREQUAL",
    163 to "OP_MIN",
    164 to "OP_MAX",
    165 to "OP_WITHIN",
    166 to "OP_RIPEMD160",
    167 to "OP_SHA1",
    168 to "OP_SHA256",
    169 to "OP_HASH160",
    170 to "OP_HASH256",
    171 to "OP_CODESEPARATOR",
    172 to "OP_CHECKSIG",
    173 to "OP_CHECKSIGVERIFY",
    174 to "OP_CHECKMULTISIG",
    175 to "OP_CHECKMULTISIGVERIFY",
    176 to "OP_NOP1",
    177 to "OP_CHECKLOCKTIMEVERIFY",
    178 to "OP_CHECKSEQUENCEVERIFY",
    179 to "OP_NOP4",
    180 to "OP_NOP5",
    181 to "OP_NOP6",
    182 to "OP_NOP7",
    183 to "OP_NOP8",
    184 to "OP_NOP9",
    185 to "OP_NOP10"
)

val OP_CODE_FUNCTIONS = mapOf(
    0 to ::op0,
    79 to ::op1Negate,
    81 to ::op1,
    82 to ::op2,
    83 to ::op3,
    84 to ::op4,
    85 to ::op5,
    86 to ::op6,
    87 to ::op7,
    88 to ::op8,
    89 to ::op9,
    90 to ::op10,
    91 to ::op11,
    92 to ::op12,
    93 to ::op13,
    94 to ::op14,
    95 to ::op15,
    96 to ::op16,
    97 to ::opNop,
    99 to ::opIf,
    100 to ::opNotIf,
    105 to ::opVerify,
    106 to ::opReturn,
    107 to ::opToAltStack,
    108 to ::opFromAltStack,
    109 to ::op2drop,
    110 to ::op2dup,
    111 to ::op3dup,
    112 to ::op2over,
    113 to ::op2rot,
    114 to ::op2swap,
    115 to ::opIfDup,
    116 to ::opDepth,
    117 to ::opDrop,
    118 to ::opDup,
    119 to ::opNip,
    120 to ::opOver,
    121 to ::opPick,
    122 to ::opRoll,
    123 to ::opRot,
    124 to ::opSwap,
    125 to ::opTuck,
    130 to ::opSize,
    135 to ::opEqual,
    136 to ::opEqualVerify,
    139 to ::op1add,
    140 to ::op1sub,
    143 to ::opNegate,
    144 to ::opAbs,
    145 to ::opNot,
    146 to ::op0NotEqual,
    147 to ::opAdd,
    148 to ::opSub,
    149 to ::opMul,
    154 to ::opBoolAnd,
    155 to ::opBoolOr,
    156 to ::opNumEqual,
    157 to ::opNumEqualVerify,
    158 to ::opNumNotEqual,
    159 to ::opLessThan,
    160 to ::opGreaterThan,
    161 to ::opLessThanOrEqual,
    162 to ::opGreaterThanOrEqual,
    163 to ::opMin,
    164 to ::opMax,
    165 to ::opWithin,
    166 to ::opRipemd160,
    167 to ::opSha1,
    168 to ::opSha256,
    169 to ::opHash160,
    170 to ::opHash256,
    172 to ::opCheckSig,
    173 to ::opCheckSigVerify,
    174 to ::opCheckMultiSig,
    175 to ::opCheckMultiSigVerify,
    176 to ::opNop,
    177 to ::opCheckLockTimeVerify,
    178 to ::opCheckSequenceVerify,
    179 to ::opNop,
    180 to ::opNop,
    181 to ::opNop,
    182 to ::opNop,
    183 to ::opNop,
    184 to ::opNop,
    185 to ::opNop
)