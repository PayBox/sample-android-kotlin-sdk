package money.paybox.samplekotlin

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.button.ButtonConstants.ButtonTheme
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import com.google.android.material.snackbar.Snackbar
import money.paybox.payboxsdk.PayboxSdk
import money.paybox.payboxsdk.config.Language
import money.paybox.payboxsdk.config.PaymentSystem
import money.paybox.payboxsdk.config.Region
import money.paybox.payboxsdk.config.RequestMethod
import money.paybox.payboxsdk.interfaces.WebListener
import money.paybox.payboxsdk.view.PaymentView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), WebListener {
    lateinit var loaderView: View
    lateinit var outputTextView: TextView
    lateinit var paymentView: PaymentView

    //Необходимо заменить тестовый secretKey и merchantId на свой
    private val secretKey = "mQKzUjrDqdIxViLJ"
    private val merchantId = 550624
    private val googleMerchantId = "BCR2DN6T57R772SF"
    private val gateway = "payboxmoney"
    private val gatewayMerchantId = "paybox_pp"
    private val kzCountryCode = "KZ"
    private val ruCountryCode = "RU"
    private val currency = "KZT"

    //Если email или phone не указан, то выбор будет предложен на сайте платежного гейта
    private val email = "user@mail.com"
    private val phone = "77012345678"
    private var url: String? = null
    val sdk by lazy { PayboxSdk.initialize(merchantId, secretKey) }
    private val allowedCardNetworks = JSONArray(
        listOf(
            "AMEX",
            "DISCOVER",
            "INTERAC",
            "JCB",
            "MASTERCARD",
            "VISA"
        )
    )

    private val allowedCardAuthMethods = JSONArray(
        listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loaderView = findViewById(R.id.loaderView)
        outputTextView = findViewById(R.id.outputTextView)
        paymentView = findViewById(R.id.paymentView)
        ViewCompat.setTranslationZ(paymentView, 10f)

        val googlePayButton: PayButton = findViewById(R.id.buttonPaymentByGoogle)

        val googlePaymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .setTheme(WalletConstants.THEME_LIGHT)
                .build()
        )
        googlePayButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonTheme(ButtonTheme.LIGHT)
                .setButtonType(ButtonType.BUY)
                .setCornerRadius(100)
                .setAllowedPaymentMethods(JSONArray().put(cardPaymentMethod()).toString())
                .build()
        )

        sdk.setPaymentView(paymentView)
        paymentView.listener = this
        sdk.config().testMode(true)  //По умолчанию тестовый режим включен
        //Выбор региона
        sdk.config().setRegion(Region.DEFAULT) //По умолчанию установлен Region.DEFAULT
        //Выбор платежной системы:
        sdk.config().setPaymentSystem(PaymentSystem.NONE)
        //Выбор валюты платежа:
        sdk.config().setCurrencyCode("KZT")
        //Активация автоклиринга:
        sdk.config().autoClearing(true)
        //Установка кодировки:
        sdk.config().setEncoding("UTF-8") //по умолчанию UTF-8
        //Время жизни рекурентного профиля:
        sdk.config().setRecurringLifetime(1) //по умолчанию 36 месяцев
        //Время жизни платежной страницы, в течение которого платеж должен быть завершен:
        sdk.config().setPaymentLifetime(300)  //по умолчанию 300 секунд
        //Включение режима рекурентного платежа:
        sdk.config().recurringMode(false)  //по умолчанию отключен
        //Номер телефона клиента, будет отображаться на платежной странице. Если не указать, то будет предложено ввести на платежной странице:
        sdk.config().setUserPhone(phone)
        //Email клиента, будет отображаться на платежной странице. Если не указать email, то будет предложено ввести на платежной странице:
        sdk.config().setUserEmail(email)
        //Язык платежной страницы:
        sdk.config().setLanguage(Language.ru)
        //Для передачи информации от платежного гейта:
        sdk.config().setCheckUrl("http://test.paybox.kz/")
        sdk.config().setResultUrl("http://test.paybox.kz/")
        sdk.config().setRefundUrl("http://test.paybox.kz/")
        sdk.config().setClearingUrl("http://test.paybox.kz/")
        sdk.config().setRequestMethod(RequestMethod.GET)
        //Для выбора Frame вместо платежной страницы
        sdk.config().setFrameRequired(true) //false по умолчанию

        //Инициализация нового платежа
        findViewById<Button>(R.id.buttonInitPayment).setOnClickListener {
            val amount = 10f
            val description = "some description"
            val orderId = "1234"
            val userId = "1234"
            val extraParams = null

            outputTextView.text = ""
            paymentView.visibility = View.VISIBLE

            sdk.createPayment(amount, description, orderId, userId, extraParams) { payment, error ->
                Log.e("initPAY", error?.description ?: "")
                paymentView.visibility = View.GONE
            }
        }

        findViewById<Button>(R.id.buttonDirectPayment).setOnClickListener {
            val amount = 10f
            val userId = "1234"
            val cardToken = "your_card_token"
            val description = "some description"
            val orderId = "1234"
            val extraParams = null

            outputTextView.text = ""
            paymentView.visibility = View.VISIBLE

            sdk.createCardPayment(
                amount,
                userId,
                cardToken,
                description,
                orderId,
                extraParams
            ) { payment, error ->
                Log.e("initDirectPAY", error?.description ?: "")

                sdk.createNonAcceptancePayment(payment?.paymentId ?: 0) { payment2, error2 ->
                    Log.e("makeDirectPAY", error2?.description ?: "")
                    Log.e("initPAY", payment2?.status ?: "")
                }

                paymentView.visibility = View.GONE
            }
        }

        //Отображение списка привязанных карт
        findViewById<Button>(R.id.buttonShowCards).setOnClickListener {
            val userId = "1234"

            outputTextView.text = ""
            loaderView.visibility = View.VISIBLE

            sdk.getAddedCards(userId) { cards, error ->
                run {
                    loaderView.visibility = View.GONE

                    if (error != null) {
                        showError(error.description)

                        return@run
                    }

                    if (cards != null) {

                        if (cards.size == 0) {
                            showError("Список пуст")

                            return@run
                        }

                        var message = String()

                        for (card in cards) {
                            message += """
                                Card hash = ${card.cardhash}
                                Card ID = ${card.cardId}
                                Recurring profile = ${card.recurringProfile}
                                Token = ${card.cardToken}
                                Created At = ${card.date}
                                Status = ${card.status}
                                """.trimIndent()
                        }

                        outputTextView.text = message
                    }
                }
            }
        }

        //Привязка новой карты
        findViewById<Button>(R.id.buttonAddCard).setOnClickListener {
            val userId = "1234"
            //postUrl - для обратной связи
            val postUrl = "http://test.paybox.kz/"

            outputTextView.text = ""
            paymentView.visibility = View.VISIBLE

            sdk.addNewCard(userId, postUrl) { payment, error ->
                run {
                    paymentView.visibility = View.GONE

                    if (error != null) {
                        showError(error.description)

                        return@run
                    }

                    outputTextView.text = """
                        Payment ID = ${payment!!.paymentId.toString()}
                        Status = ${payment!!.status}
                        """.trimIndent()
                }
            }
        }

        //Удаление привязанной карты по ID
        findViewById<Button>(R.id.buttonDeleteCard).setOnClickListener {
            val userId = "1234"

            val alert = AlertDialog.Builder(this)
            val edittext = EditText(this)
            edittext.inputType = InputType.TYPE_CLASS_NUMBER
            alert.setMessage("Введите card ID")
            alert.setTitle("Удаление карты")

            alert.setView(edittext)

            alert.setPositiveButton(
                "Удалить"
            ) { dialog: DialogInterface?, whichButton: Int ->
                try {
                    val cardId = edittext.text.toString().toInt()
                    outputTextView.text = ""
                    loaderView.visibility = View.VISIBLE

                    sdk.removeAddedCard(cardId, userId) { card, error ->
                        kotlin.run {
                            loaderView.visibility = View.GONE

                            if (error != null) {
                                showError(error.description)

                                return@run
                            }

                            outputTextView.text = """
                                Deleted At = ${card!!.date}
                                Status = ${card!!.status}
                                """.trimIndent()
                        }
                    }
                } catch (e: Exception) {
                    showError("Введите целочисленное значение")
                }
            }

            alert.setNegativeButton(
                "Отмена"
            ) { dialog: DialogInterface?, whichButton: Int -> }

            alert.show()
        }
        // Создание платежа через Google Pay
        googlePayButton.setOnClickListener {
            val amount = 10f
            val description = "some description"
            val orderId = "1234"
            val userId = "1234"
            val extraParams = null
            sdk.createGooglePayment(
                amount,
                description,
                orderId,
                userId,
                extraParams
            ) { payment, error ->
                url = payment?.redirectUrl.toString()
                val paymentDataRequestJson = getPaymentDataRequest("10")
                val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
                AutoResolveHelper.resolveTask<PaymentData>(
                    googlePaymentsClient.loadPaymentData(request),
                    this,
                    REQUEST_CODE
                )
            }
        }
    }

    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }
            put("type", "CARD")
            put("parameters", parameters)
        }
    }

    private fun cardPaymentMethod(): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification())

        return cardPaymentMethod
    }

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters", JSONObject(
                    mapOf(
                        "gateway" to gateway,
                        "gatewayMerchantId" to gatewayMerchantId
                    )
                )
            )
        }
    }

    private fun getTransactionInfo(price: String): JSONObject {
        return JSONObject().apply {
            put("totalPrice", price)
            put("totalPriceStatus", "FINAL")
            put("countryCode", kzCountryCode)
            put("currencyCode", currency)
        }
    }

    private val merchantInfo: JSONObject =
        JSONObject().put("merchantId", googleMerchantId)

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    fun getPaymentDataRequest(price: String): JSONObject? {
        try {
            return baseRequest.apply {
                put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod()))
                put("transactionInfo", getTransactionInfo(price))
                put("merchantInfo", merchantInfo)
                val shippingAddressParameters = JSONObject().apply {
                    put("phoneNumberRequired", false)
                    put("allowedCountryCodes", JSONArray(listOf(kzCountryCode, ruCountryCode)))
                }
                put("shippingAddressParameters", shippingAddressParameters)
                put("shippingAddressRequired", true)
            }
        } catch (e: JSONException) {
            return null
        }
    }

    private fun showError(text: String) {
        val snackbar: Snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            text,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.duration = 5000
        snackbar.show()
    }

    override fun onLoadFinished() {
        loaderView.visibility = View.GONE
    }

    override fun onLoadStarted() {
        loaderView.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (paymentView.isVisible) {
            finish()
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val REQUEST_CODE = 123
        const val OK = "ok"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data == null)
                            return
                        val paymentData = PaymentData.getFromIntent(data)
                        val jsonObject = JSONObject(paymentData?.toJson())
                        val paymentMethodData = jsonObject.getJSONObject("paymentMethodData")
                        val tokenizationData = paymentMethodData.getJSONObject(
                            "tokenization" + "Data"
                        )
                        val token = tokenizationData.getString("token")
                        url?.let {
                            sdk.confirmGooglePayment(it, token) { payment, error ->
                                if (payment?.status == OK) {
                                    showError("Ваш платеж успешно выполнен")
                                } else {
                                    error?.let { showError(it?.description) }
                                }
                            }
                        }
                    }

                    Activity.RESULT_CANCELED -> {
                        showError("Платеж был отменен")
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        if (data == null)
                            return
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        status?.statusMessage?.let { showError(it) }
                    }

                    else -> {}
                }
            }

            else -> {}
        }
    }
}