package script

import extension.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger.ONE
import java.math.BigInteger.TWO
import java.util.*

class OperationKtTest : StringSpec({

    "should verify if number in last of stack is different to ZERO and remove it" {
        val stack = getStack()

        opVerify(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
        }
    }

    "should take the last element in a stack and put in other" {
        val stack = getStack()
        val altStack = Stack<ByteArray>()

        opToAltStack(stack, altStack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
        }
        altStack shouldBe getEmptyStack().apply {
            add(byteArrayOf(6))
        }
    }

    "should take the last element in altStack and put on stack" {
        val stack = getStack()
        val altStack = getStack()

        opFromAltStack(stack, altStack)

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(6))
        }
        altStack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
        }
    }

    "should drop the last two elements" {
        val stack = getStack()

        op2drop(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
        }
    }

    "should duplicate two last elements" {
        val stack = getStack()

        op2dup(stack) shouldBe true

        stack shouldBe getStack().apply {
            add(byteArrayOf(5))
            add(byteArrayOf(6))
        }
    }

    "should duplicate three last elements" {
        val stack = getStack()

        op3dup(stack) shouldBe true

        stack shouldBe getStack().apply {
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
        }
    }

    "should take fourth and second last elements and put in the end" {
        val stack = getStack()

        op2over(stack) shouldBe true

        stack shouldBe getStack().apply {
            add(byteArrayOf(3))
            add(byteArrayOf(4))
        }
    }

    "should take sixth and fourth last elements and put in the end" {
        val stack = getStack()

        op2rot(stack) shouldBe true

        stack shouldBe getStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
        }
    }

    "should swap the last four elements" {
        val stack = getStack()

        op2swap(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))

            add(byteArrayOf(5))
            add(byteArrayOf(6))

            add(byteArrayOf(3))
            add(byteArrayOf(4))
        }
    }

    "should duplicate last is the last is different of ZERO" {
        val stack = getStack()

        opIfDup(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(6))
        }
    }

    "should put the size of the stack in the stack" {
        val stack = getStack()

        opDepth(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(6)) //size
        }
    }

    "should drop the last element" {
        val stack = getStack()

        opDrop(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
        }
    }

    "should duplicate the last element" {
        val stack = getStack()

        opDup(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(6))
        }
    }

    "should remove the penultimate element" {
        val stack = getStack()

        opNip(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(6))
        }
    }

    "should duplicate the penultimate element and put in the end" {
        val stack = getStack()

        opOver(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(5))
        }
    }

    "should pick the last index reference" {
        val stack = getEmptyStack().apply {
            val reference = byteArrayOf(3)
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(reference)
        }

        opPick(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(3))
        }
    }

    "should move element in the reference index to end" {
        val stack = getEmptyStack().apply {
            val reference = byteArrayOf(4)
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(reference)
        }

        opRoll(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(2))
        }
    }

    "should move third last element and put in the end" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
        }

        opRot(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(4))
        }
    }

    "should swap last and penultimate" {
        val stack = getStack()

        opSwap(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(6))
            add(byteArrayOf(5))
        }
    }

    "should last element penultimate" {
        val stack = getStack()

        opTuck(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))

            add(byteArrayOf(6))

            add(byteArrayOf(5))
            add(byteArrayOf(6))
        }
    }

    "should put the size of the last element in the stack" {
        val stack = getStack().apply {
            add(byteArrayOf(1, 2, 3))
        }

        opSize(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(6))
            add(byteArrayOf(1, 2, 3))

            add(byteArrayOf(3))
        }
    }

    "should compare the two lasts elements, and put 1 if they are equals and empty if different" {
        val stackDifferentFinals = getStack()

        opEqual(stackDifferentFinals) shouldBe true

        stackDifferentFinals shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf())
        }

        val stackEqualFinals = getStack().apply {
            add(byteArrayOf(6))
        }

        opEqual(stackEqualFinals) shouldBe true

        stackEqualFinals shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(1))
        }
    }

    "should run opEqual and opVerify" {
        val stackDifferentFinals = getStack()

        opEqualVerify(stackDifferentFinals) shouldBe false

        stackDifferentFinals shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
        }

        val stackEqualFinals = getStack().apply {
            add(byteArrayOf(6))
        }

        opEqualVerify(stackEqualFinals) shouldBe true

        stackEqualFinals shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
        }
    }

    "should add ONE to the last element" {
        val stack = getStack()

        op1add(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(7))
        }
    }

    "should sub ONE to the last element" {
        val stack = getStack()

        op1sub(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(5))
        }
    }

    "should negate the last element" {
        val stack = getStack()

        opNegate(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
            add(byteArrayOf(4))
            add(byteArrayOf(5))
            add(byteArrayOf(-6))
        }
    }

    "should put abs value of the last element" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(-122))
        }

        opAbs(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(122))
        }
    }

    "should put one if the last element is Zero or Zero if not" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(0))
        }

        opNot(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack1 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        opNot(stack1) shouldBe true

        stack1 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }
    }

    "should put ZERO if the last element is Zero or ONE if not" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(0))
        }

        op0NotEqual(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }

        val stack1 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
        }

        op0NotEqual(stack1) shouldBe true

        stack1 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should sum the last two elements" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
        }

        opAdd(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(5))
        }
    }

    "should sub the last two elements" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
        }

        opSub(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should multiply the last two elements" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
        }

        opMul(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(6))
        }
    }

    "should verify if the last two elements are different of Zero" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(3))
        }

        opBoolAnd(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(0))
        }

        opBoolAnd(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }
    }

    "should verify if one of the two elements are different of Zero" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(0))
        }

        opBoolOr(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(0))
            add(byteArrayOf(0))
        }

        opBoolOr(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }
    }

    "should verify if the last two elements are equals" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(2))
        }

        opNumEqual(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(4))
        }

        opNumEqual(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }
    }

    "should verify if the last two elements are equals and run opVerify" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(4))
            add(byteArrayOf(2))
            add(byteArrayOf(2))
        }

        opNumEqualVerify(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(4))
        }

        val stack1 = getEmptyStack().apply {
            add(byteArrayOf(4))
            add(byteArrayOf(3))
            add(byteArrayOf(2))
        }

        opNumEqualVerify(stack1) shouldBe false

        stack1 shouldBe getEmptyStack().apply {
            add(byteArrayOf(4))
        }
    }

    "should verify if the last two elements are different" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(2))
        }

        opNumNotEqual(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(4))
        }

        opNumNotEqual(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should verify if the penultimate is less than last" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(10))
        }

        opLessThan(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(10))
            add(byteArrayOf(4))
        }

        opLessThan(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }
    }

    "should verify if the penultimate is greater than last" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(10))
        }

        opGreaterThan(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(10))
            add(byteArrayOf(4))
        }

        opGreaterThan(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should verify if the penultimate is less than last or equal" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        opLessThanOrEqual(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(2))
            add(byteArrayOf(4))
        }

        opLessThanOrEqual(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should verify if the penultimate is greater than last or equal" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
            add(byteArrayOf(10))
        }

        opGreaterThanOrEqual(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf())
        }

        val stack2 = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(10))
            add(byteArrayOf(10))
        }

        opGreaterThanOrEqual(stack2) shouldBe true

        stack2 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(1))
        }
    }

    "should get the min between the two last elements" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(3))
            add(byteArrayOf(10))
        }

        opMin(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(3))
        }
    }

    "should get the max between the two last elements" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(3))
            add(byteArrayOf(10))
        }

        opMax(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(10))
        }
    }

    "should verify if the element is within others two before" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(1))
            add(byteArrayOf(3))
            add(byteArrayOf(10))
        }

        opWithin(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(byteArrayOf())
        }

        val stack1 = getEmptyStack().apply {
            add(byteArrayOf(4))
            add(byteArrayOf(3))
            add(byteArrayOf(10))
        }

        opWithin(stack1) shouldBe true

        stack1 shouldBe getEmptyStack().apply {
            add(byteArrayOf(1))
        }
    }

    "should ripemd160 in the element" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(10))
        }

        opRipemd160(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(hash160(byteArrayOf(10)))
        }
    }

    "should sha1 in the element" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(10))
        }

        opSha1(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(sha1(byteArrayOf(10)))
        }
    }

    "should sha256 in the element" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(10))
        }

        opSha256(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(sha256(byteArrayOf(10)))
        }
    }

    "should hash160 in the element" {
        val stack = getEmptyStack().apply {
            add("hello world".toByteArray())
        }

        opHash160(stack) shouldBe true

        stack[0].toHex() shouldBe "d7d5ee7824ff93f94c3055af9382c86c68b5ca92"
    }

    "should hash256 in the element" {
        val stack = getEmptyStack().apply {
            add(byteArrayOf(10))
        }

        opHash256(stack) shouldBe true

        stack shouldBe getEmptyStack().apply {
            add(hash256(byteArrayOf(10)))
        }
    }

    "should check locktime" {
        var stack = getStack()

        var locktime = 50000.toBigInteger()
        var sequence = 0xffffffff.toBigInteger()
        opCheckLockTimeVerify(stack, locktime, sequence) shouldBe false

        stack = getEmptyStack().apply {
            add(byteArrayOf(-1))
        }
        sequence = 4.toBigInteger()
        opCheckLockTimeVerify(stack, locktime, sequence) shouldBe false

        stack = getEmptyStack().apply {
            add(644_656.toBigInteger().toByteArray())
        }
        locktime = 500_000_001.toBigInteger()
        opCheckLockTimeVerify(stack, locktime, sequence) shouldBe false

        stack = getEmptyStack().apply {
            add(644656.toBigInteger().toLittleEndianByteArray())
        }
        locktime = 644000.toBigInteger()
        opCheckLockTimeVerify(stack, locktime, sequence) shouldBe false

    }

    "should check sequence" {
        var stack = getEmptyStack().apply {
            add(644656.toBigInteger().toLittleEndianByteArray())
        }

        var version = 1
        var sequence = ONE.negate()
        opCheckSequenceVerify(stack, version, sequence) shouldBe false

        stack = getEmptyStack().apply {
            add(2147483648.toBigInteger().toByteArray().reversedArray())
        }
        version = 1
        sequence = ONE
        opCheckSequenceVerify(stack, version, sequence) shouldBe false

        version = 1
        sequence = TWO
        opCheckSequenceVerify(stack, version, sequence) shouldBe false

    }

    "should check signature" {
        val z = "7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d".toBigInteger(16)
        val sec = ("04887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e" +
                "4da568744d06c61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34").decodeHex()
        val sig = ("3045022000eff69ef2b1bd93a66ed5219add4fb51e11a840f404876" +
                "325a1e8ffe0529a2c022100c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab601").decodeHex()
        val stack = getEmptyStack().apply {
            add(sig)
            add(sec)
        }

        opCheckSig(stack, z) shouldBe true
        stack[0].littleEndianToBigInteger() shouldBe ONE
    }

})

fun getStack(): Stack<ByteArray> {
    val stack = Stack<ByteArray>()
    stack.add(byteArrayOf(1))
    stack.add(byteArrayOf(2))
    stack.add(byteArrayOf(3))
    stack.add(byteArrayOf(4))
    stack.add(byteArrayOf(5))
    stack.add(byteArrayOf(6))
    return stack
}

fun getEmptyStack() = Stack<ByteArray>()
