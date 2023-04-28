import ellipticcurve.S256Point
import extension.SIGHASH_ALL
import extension.decodeHex
import extension.hash256
import extension.toBigInteger
import extension.toHex
import extension.toHex64
import extension.toLittleEndianByteArray
import extension.toVarint
import script.Script
import script.p2pkhScriptPubkey
import signature.PrivateKey
import signature.Signature
import transaction.Transaction
import transaction.TransactionInput
import transaction.TransactionOutput

fun testTransaction() {
    val privateKey = PrivateKey("igor.chagas.rodrigues@gmail.com")
    val changeAddress = privateKey.publicKey.address(testNet = true)
    val targetAddress = "mwJn1YPMq7y5F8J3LkC5Hxg9PHyZ5K4cFv"

    val input1 = TransactionInput(
        previousTransactionId = "8319d65a3b0d91623e537789429b87cb3e01bdb804e193ee521e4c64a40c44fd".decodeHex(),
        previousIndex = 1.toBigInteger()
    )

    val input2 = TransactionInput(
        previousTransactionId = "6c46f7bd9a08dff18d913c630f4ebd29f2d6715d7513d49fa449c92015531dae".decodeHex(),
        previousIndex = 0.toBigInteger()
    )

    val change = TransactionOutput(
        amount = 2000000.toBigInteger(),
        scriptPubKey = p2pkhScriptPubkey(changeAddress)
    )

    val target = TransactionOutput(
        amount = 961000.toBigInteger(),
        scriptPubKey = p2pkhScriptPubkey(targetAddress)
    )

    val transaction = Transaction(
        version = 2.toBigInteger(),
        inputs = listOf(input1, input2),
        outputs = listOf(target, change),
        testnet = true
    )

    if (transaction.signInput(0, privateKey) && transaction.signInput(1, privateKey)) {
        println(transaction)
        println(transaction.id())
        println(transaction.fee())
        println(transaction.serialize().toHex())
    } else {
        throw IllegalArgumentException("erro")
    }
}

fun testP2SHExample() {
    val transactionHex =
        "0100000001868278ed6ddfb6c1ed3ad5f8181eb0c7a385aa0836f01d5e4789e6" +
                "bd304d87221a000000db00483045022100dc92655fe37036f47756db8102e0d7d5e28b3beb83a8" +
                "fef4f5dc0559bddfb94e02205a36d4e4e6c7fcd16658c50783e00c341609977aed3ad00937bf4e" +
                "e942a8993701483045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef" +
                "53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e754022" +
                "01475221022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb702103" +
                "b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb7152aeffffffff04" +
                "d3b11400000000001976a914904a49878c0adfc3aa05de7afad2cc15f483a56a88ac7f40090000" +
                "0000001976a914418327e3f3dda4cf5b9089325a4b95abdfa0334088ac722c0c00000000001976" +
                "a914ba35042cfe9fc66fd35ac2224eebdafd1028ad2788acdc4ace020000000017a91474d691da" +
                "1574e6b3c192ecfb52cc8984ee7b6c568700000000"
    val hexSec = "03b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb71"
    val hexDer =
        "3045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e754022"
    val hexRedeemScript =
        "475221022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb702103b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb7152ae"

    val sec = hexSec.decodeHex()
    val der = hexDer.decodeHex()
    val redeemScript = hexRedeemScript.decodeHex().let { Script.parse(it.inputStream()) }

    val transaction = Transaction.parse(transactionHex.decodeHex().inputStream())
    println(transaction.verify())
    transaction.sigHash(0, redeemScript)

    val i = transaction.inputs[0]
    val s = transaction.version.toLittleEndianByteArray().copyOf(4).let {
        it + transaction.inputs.size.toBigInteger().toVarint()
    }.let {
        it + TransactionInput(
            previousIndex = i.previousIndex,
            previousTransactionId = i.previousTransactionId,
            scriptSignature = redeemScript,
            sequence = i.sequence
        ).serialize()
    }.let {
        it + transaction.outputs.size.toBigInteger().toVarint()
    }.let {
        it + transaction.outputs.fold(byteArrayOf()) { acc, transactionOutput -> acc + transactionOutput.serialize() }
    }.let {
        it + transaction.lockTime.toLittleEndianByteArray().copyOf(4)
    }.let {
        it + SIGHASH_ALL.toByteArray().copyOf(4)
    }
    val z = hash256(s).toBigInteger()
    println(z.toHex64())
    val point = S256Point.parse(sec)
    val signature = Signature.parse(der)
    println(point.verify(z, signature))
}