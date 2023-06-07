package transaction

import extension.decodeHex
import extension.toHex
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import signature.PrivateKey
import java.math.BigInteger

class TransactionTest : StringSpec({

    val transaction = Transaction.parse(TRANSACTION_HEX.decodeHex().inputStream())
    val previousTransaction = Transaction.parse(PREVIOUS_TRANSACTION_HEX.decodeHex().inputStream())

    beforeTest {
        mockkObject(TransactionFetcher.Companion)
        every { TransactionFetcher.Companion.fetch(PREVIOUS_TRANSACTION_ID) } answers { previousTransaction }
    }

    "should parse version number" {
        transaction.version shouldBe 1.toBigInteger()
    }

    "should parse transaction inputs" {
        transaction.inputs.size shouldBe 1
        val transactionInput = transaction.inputs.component1()
        transactionInput.previousTransactionId shouldBe PREVIOUS_TRANSACTION_ID.decodeHex()
        transactionInput.previousIndex shouldBe BigInteger.ZERO
        transactionInput.scriptSignature.serialize() shouldBe SIG_SCRIPT.decodeHex()
        transactionInput.sequence shouldBe SEQUENCE
    }

    "should parse transaction outputs" {
        transaction.outputs.size shouldBe 2
        val transactionOutput1 = transaction.outputs.component1()
        transactionOutput1.amount shouldBe 32454049.toBigInteger()
        transactionOutput1.scriptPubKey.serialize() shouldBe "1976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac".decodeHex()

        val transactionOutput2 = transaction.outputs.component2()
        transactionOutput2.amount shouldBe 10011545.toBigInteger()
        transactionOutput2.scriptPubKey.serialize() shouldBe "1976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac".decodeHex()
    }

    "should parse lockTime" {
        transaction.lockTime shouldBe 410393.toBigInteger()
    }

    "should serialize transaction" {
        transaction.serialize().toHex() shouldBe TRANSACTION_HEX
    }

    "should calculation the fee of the transaction" {
        transaction.fee() shouldBe 40000.toBigInteger()
    }

    "should calculate signature hash from a input transaction" {
        val transactionRetrieve = getTransaction(
            "452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03",
            false
        )
        transactionRetrieve.sigHash(0) shouldBe
                BigInteger("27e0c5994dec7824e56dec6b2fcb342eb7cdb0d0957c2fce9882f715e85d81a6", 16)
    }

    "should verify Input signature" {
        transaction.verifyInput(0) shouldBe true
    }

    "should verify a transaction p2pkh" {
        val transaction1 = getTransaction("452c629d67e41baec3ac6f04fe744b4b9617f8f859c63b3002f8684e7a4fee03")
        transaction1.verify() shouldBe true
        val transaction2 = getTransaction("5418099cc755cb9dd3ebc6cf1a7888ad53a1a3beb5a025bce89eb1bf7f1650a2", testnet = true)
        transaction2.verify() shouldBe true
    }

    "should verify a transaction p2sh" {
        val transaction1 = getTransaction("46df1a9484d0a81d03ce0ee543ab6e1a23ed06175c104a178268fad381216c2b")
        transaction1.verify() shouldBe true
    }

    "should sign an input" {
        val privateKey = PrivateKey(8675309.toBigInteger())
        val transactionToSign = Transaction.parse(NO_SIGN_TRANSACTION_HEX.decodeHex().inputStream(), testnet = true)
        transactionToSign.signInput(0, privateKey) shouldBe true
        transactionToSign.verify() shouldBe true
    }

    "should return if is a coinbase transaction" {
        Transaction.parse(COINBASE_TRANSACTION.decodeHex().inputStream()).isCoinbase() shouldBe true
        Transaction.parse(TRANSACTION_HEX.decodeHex().inputStream()).isCoinbase() shouldBe false
    }

    "should return the height block from coinbase transaction" {
        Transaction.parse(COINBASE_TRANSACTION.decodeHex().inputStream()).coinbaseHeight() shouldBe 465879.toBigInteger()
    }

    "should return the height block equal -1 when the transaction is not a coinbase transaction" {
        Transaction.parse(TRANSACTION_HEX.decodeHex().inputStream()).coinbaseHeight() shouldBe BigInteger.ONE.negate()
    }
})

private fun getTransaction(transactionId: String, testnet: Boolean = false) = TransactionFetcher
    .fetch(transactionId, testnet)

private const val NO_SIGN_TRANSACTION_HEX =
    "010000000199a24308080ab26e6fb65c4eccfadf76749bb5bfa8cb08f291320b3c21e56f0d0d00000000ffffffff02408af701000000001976" +
            "a914d52ad7ca9b3d096a38e752c2018e6fbc40cdf26f88ac80969800000000001976a914507b27411ccf7f16f10297de6cef3f2916" +
            "23eddf88ac00000000"

private const val TRANSACTION_HEX =
    "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100e" +
            "d81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c" +
            "31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef010" +
            "00000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb7" +
            "5f40df79fea1288ac19430600"

private const val PREVIOUS_TRANSACTION_ID = "d1c789a9c60383bf715f3f6ad9d14b91fe55f3deb369fe5d9280cb1a01793f81"

private const val SIG_SCRIPT =
    "6b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35" +
            "d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138b" +
            "d94bdd531d2e213bf016b278a"

private const val PREVIOUS_TRANSACTION_HEX = "0100000002137c53f0fb48f83666fcfd2fe9f12d13e94ee109c5aeabbfa32bb9e02538f4" +
        "cb000000006a47304402207e6009ad86367fc4b166bc80bf10cf1e78832a01e9bb491c6d126ee8aa436cb502200e29e6dd7708ed419cd" +
        "5ba798981c960f0cc811b24e894bff072fea8074a7c4c012103bc9e7397f739c70f424aa7dcce9d2e521eb228b0ccba619cd6a0b9691d" +
        "a796a1ffffffff517472e77bc29ae59a914f55211f05024556812a2dd7d8df293265acd8330159010000006b483045022100f4bfdb0b3" +
        "185c778cf28acbaf115376352f091ad9e27225e6f3f350b847579c702200d69177773cd2bb993a816a5ae08e77a6270cf46b33f8f79d4" +
        "5b0cd1244d9c4c0121031c0b0b95b522805ea9d0225b1946ecaeb1727c0b36c7e34165769fd8ed860bf5ffffffff027a9588020000000" +
        "01976a914a802fc56c704ce87c42d7c92eb75e7896bdc41ae88aca5515e00000000001976a914e82bd75c9c662c3f5700b33fec8a676b" +
        "6e9391d588ac00000000"

private val SEQUENCE = 0xfffffffe.toBigInteger()