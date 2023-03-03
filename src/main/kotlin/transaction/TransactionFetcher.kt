package transaction

import com.github.kittinunf.fuel.httpGet
import configuration.MAIN_NET_URL
import configuration.TEST_NET_URL
import extension.decodeHex
import extension.littleEndianToBigInteger

class TransactionFetcher {

    companion object {
        private val cache = mutableMapOf<String, Transaction>()

        fun fetch(
            transactionId: String,
            testnet: Boolean = false,
            fresh: Boolean = false
        ) = if (fresh || thereNoInCache(transactionId)) retrieveTransactionAndPutInCache(
            transactionId,
            testnet
        ) else cache[transactionId]!!

        private fun retrieveTransactionAndPutInCache(transactionId: String, testnet: Boolean) =
            getTransactionHex(testnet, transactionId).let { hex ->
                parseTransaction(hex, testnet).let { transaction ->
                    validateTransaction(transaction, transactionId)
                    putInCache(transaction)
                    transaction
                }
            }

        private fun thereNoInCache(transactionId: String) = cache.none { it.key == transactionId }

        private fun getTransactionHex(testnet: Boolean, transactionId: String) =
            "${getBaseUrl(testnet)}/api/tx/$transactionId/hex".httpGet().responseString().third.get()

        private fun getBaseUrl(testnet: Boolean = false) = if (testnet) TEST_NET_URL else MAIN_NET_URL

        private fun parseTransaction(hex: String, testnet: Boolean): Transaction {
            val raw = hex.decodeHex()
            return if (markIsDifferent(raw)) {
                modifyRawAndParse(raw, testnet)
            } else Transaction.parse(raw.inputStream(), testnet)
        }

        private fun markIsDifferent(raw: ByteArray) = raw[4] == 0.toByte()

        private fun modifyRawAndParse(raw: ByteArray, testnet: Boolean): Transaction {
            val rawModify = raw.copyOfRange(0, 4) + raw.copyOfRange(6, raw.size)
            val transaction = Transaction.parse(rawModify.inputStream(), testnet)
            return transaction.copy(
                lockTime = rawModify.copyOfRange(rawModify.size - 4, rawModify.size).littleEndianToBigInteger()
            )
        }

        private fun validateTransaction(transaction: Transaction, transactionId: String) {
            if (transaction.id() != transactionId)
                throw RuntimeException("not the same id: ${transaction.id()} vs $transactionId")
        }

        private fun putInCache(
            transaction: Transaction
        ) {
            cache[transaction.id()] = transaction
        }
    }
}