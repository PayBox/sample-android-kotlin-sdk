package money.paybox.samplekotlin

import android.content.DialogInterface
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
import com.google.android.material.snackbar.Snackbar
import money.paybox.payboxsdk.PayboxSdk
import money.paybox.payboxsdk.config.Language
import money.paybox.payboxsdk.config.PaymentSystem
import money.paybox.payboxsdk.config.RequestMethod
import money.paybox.payboxsdk.interfaces.WebListener
import money.paybox.payboxsdk.view.PaymentView
import java.lang.Exception

class MainActivity : AppCompatActivity(), WebListener {
    lateinit var loaderView: View
    lateinit var outputTextView: TextView
    lateinit var paymentView: PaymentView

    //Необходимо заменить тестовый secretKey и merchantId на свой
    private val secretKey = "UnPLLvWsuXPyC3wd"
    private val merchantId = 503623

    //Если email или phone не указан, то выбор будет предложен на сайте платежного гейта
    private val email = "user@mail.com"
    private val phone = "77012345678"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loaderView = findViewById(R.id.loaderView)
        outputTextView = findViewById(R.id.outputTextView)

        paymentView = findViewById(R.id.paymentView)
        ViewCompat.setTranslationZ(paymentView, 10f)

        val sdk = PayboxSdk.initialize(merchantId, secretKey)
        sdk.setPaymentView(paymentView)
        paymentView.listener = this
        sdk.config().testMode(true)  //По умолчанию тестовый режим включен
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

        //Инициализация нового платежа
        findViewById<Button>(R.id.buttonInitPayment).setOnClickListener {
            val amount = 10f
            val description = "some description"
            val orderId = "1234"
            val userId = "1234"
            val extraParams = null

            outputTextView.text = ""
            paymentView.visibility = View.VISIBLE

            sdk.createPayment(amount, description, orderId, userId, extraParams) {
                    payment, error -> Log.e("initPAY", error?.description ?: "")
                paymentView.visibility = View.GONE
            }
        }

        //Отображение списка привязанных карт
        findViewById<Button>(R.id.buttonShowCards).setOnClickListener {
            val userId = "1234"

            outputTextView.text = ""
            loaderView.visibility = View.VISIBLE

            sdk.getAddedCards(userId){
                    cards, error ->
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

            sdk.addNewCard(userId,postUrl) {
                    payment, error ->
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

                    sdk.removeAddedCard(cardId, userId) {
                            card, error ->
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
        if(paymentView.isVisible) {
            finish()
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }
}