package money.paybox.samplekotlin

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.CardRequirements
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

object GooglePaymentUtils {

    val SUPPORTED_NETWORKS = arrayListOf(
        WalletConstants.CARD_NETWORK_OTHER,
        WalletConstants.CARD_NETWORK_VISA,
        WalletConstants.CARD_NETWORK_MASTERCARD

    )

    val allowedPaymentMethods = arrayListOf(
        WalletConstants.PAYMENT_METHOD_CARD,
        WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
    )
    var CURRENCY_CODE = "KZT"

    const val TOKENIZATION_PUBLIC_KEY ="BOOe2Lzw5nDa/DKRdBjwNhkqnKMBK9i2k+zCAeJsXhkwdnieL9C2zd0TfM4e1rMOCftj4ix7hDfpeyZl1ZNYrZE="

    val gateway ="payboxmoney"
    val merchantId ="548178"

    fun createGoogleApiClientForPay(context: Context): PaymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .setTheme(WalletConstants.THEME_LIGHT)
                .build()
        )

    fun checkIsReadyGooglePay(
        paymentsClient: PaymentsClient,
        callback: (res: Boolean) -> Unit
    ) {
        val isReadyRequest = IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build()
        val task = paymentsClient.isReadyToPay(isReadyRequest)
        task.addOnCompleteListener {
            try {
                if (it.getResult(ApiException::class.java))
                    callback.invoke(true)
                else
                    callback.invoke(false)
            } catch (e: ApiException) {
                e.printStackTrace()
                callback.invoke(false)
            }
        }
    }

    fun createPaymentDataRequest(price: String): PaymentDataRequest {
        val transaction = createTransaction(price)
        val request = generatePaymentRequest(transaction)
        return request
    }

    fun createTransaction(price: String): TransactionInfo =
        TransactionInfo.newBuilder()
            .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
            .setTotalPrice(price)
            .setCurrencyCode(CURRENCY_CODE)
            .build()


    private fun generatePaymentRequest(transactionInfo: TransactionInfo): PaymentDataRequest {
        val tokenParams = PaymentMethodTokenizationParameters
            .newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)
            .addParameter("publicKey", TOKENIZATION_PUBLIC_KEY )
            .addParameter("gateway", gateway)
            .addParameter("pg_order_id","00102")
            .addParameter("pg_merchant_id", merchantId)
            .addParameter("pg_amount","1")
            .addParameter("pg_description","Платеж google pay")
            .addParameter("pg_salt","some random string")
            .addParameter("pg_sig","b37ec8b72f4dbb94f0900c9be5d9c7aa")
            .addParameter("pg_testing_mode","0")
            .build()

        return PaymentDataRequest.newBuilder()
            .setPhoneNumberRequired(false)
            .setEmailRequired(true)
            .setShippingAddressRequired(true)
            .setTransactionInfo(transactionInfo)
            .addAllowedPaymentMethods(allowedPaymentMethods)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(SUPPORTED_NETWORKS)
                    .setAllowPrepaidCards(true)
                    .setBillingAddressRequired(true)
                    .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                    .build()
            )
            .setPaymentMethodTokenizationParameters(tokenParams)
            .setUiRequired(true)
            .build()
    }

}