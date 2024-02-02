# sample-android-kotlin-sdk
Приложение для демонстрации работы основных функций SDK для Android приложений на Kotlin

[Скачать демонстрационный APK](https://github.com/PayBox/sample-android-kotlin-sdk/raw/master/sample-kotlin-sdk.apk)

<img src="/kotlin_init_pay.gif" width="25%" height="25%"/>

### Описание возможностей:

- Инициализация платежа
- Отмена платежа
- Возврат платежа
- Проведение клиринга
- Проведение рекуррентного платежа с сохраненными картами
- Получение информации/статуса платежа
- Добавление карт/Удаление карт
- Оплата добавленными картами
- Создание платежа с Google Pay

# **Установка:**

### 1. Добавьте репозитории Jitpack в ваш `build.gradle` на уровне проекта в конец репозиториев `allprojects`:

``` groovy
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```

### 2. Добавьте в ваш `build.gradle`:

``` groovy
dependencies {
	implementation 'com.github.PayBox:kotlin-paybox-sdk:0.10.1'
}
```

---

# Для связи с SDK

### 1. Инициализация SDK:

``` kotlin
	var sdk = PayboxSdk.initialize(merchantID, "secretKey")
```

### 2. Добавьте `PaymentView` в ваше activity:

 ```xml
 	<money.paybox.payboxsdk.view.PaymentView
            android:id="@+id/paymentView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
 ```

### 3. Передайте экземпляр paymentView в sdk:

``` kotlin
	sdk.setPaymentView(paymentView)
```

### 4. Для отслеживания прогресса загрузки платежной страницы используйте `WebListener`:

``` kotlin
    paymentView.listener = this
        
    override fun onLoadStarted() {
                
    }
    
    override fun onLoadFinished() {
                
    }    
```

---

# **Настройки SDK**

### Тестовый режим:

``` kotlin
    sdk.config().testMode(enabled)  //По умолчанию тестовый режим включен
```

### Выбор региона:

``` kotlin
    sdk.config().setRegion(Region.DEAFAULT) //Region.DEAFAULT по умолчанию
```

Класс `Region` имеет следующие значения:

| Параметр   | Значение                              |
|------------|---------------------------------------|
| `DEAFAULT` | Казахстан и другие страны присутствия |
| `RU`       | Россия                                |
| `UZ`       | Узбекистан                            |

### Выбор платежной системы:

``` kotlin
    sdk.config().setPaymentSystem(paymentSystem)
```

### Выбор валюты платежа:

``` kotlin
    sdk.config().setCurrencyCode(code)
```

### Активация автоклиринга:

``` kotlin
    sdk.config().autoClearing(enabled)
```

### Установка кодировки:

``` kotlin
    sdk.config().setEncoding(encoding) //по умолчанию UTF-8
```

### Время жизни рекурентного профиля:

``` kotlin
    sdk.config().setRecurringLifetime(lifetime) //по умолчанию 36 месяцев
```

### Время жизни платежной страницы, в течение которого платеж должен быть завершен:

``` kotlin
    sdk.config().setPaymentLifetime(lifetime)  //по умолчанию 300 секунд
```

### Включение режима рекурентного платежа:

``` kotlin
    sdk.config().recurringMode(enabled)  //по умолчанию отключен
```

### Номер телефона клиента, будет отображаться на платежной странице. Если не указать, то будет предложено ввести на платежной странице:

``` kotlin
    sdk.config().setUserPhone(userPhone)
```

### Email клиента, будет отображаться на платежной странице. Если не указать email, то будет предложено
### ввести на платежной странице:

``` kotlin
    sdk.config().setUserEmail(userEmail)
```

### Язык платежной страницы:

``` kotlin
    sdk.config().setLanguage(language)
```

### Для передачи информации от платежного гейта:

``` kotlin
    sdk.config().setCheckUrl(url)
    sdk.config().setResultUrl(url)
    sdk.config().setRefundUrl(url)
    sdk.config().setClearingUrl(url)
    sdk.config().setRequestMethod(requestMethod)
```

### Для выбора Frame вместо платежной страницы:

``` kotlin
    sdk.config().setFrameRequired(true) //false по умолчанию
```

---

# **Работа с SDK**

### Создание платежа:

``` kotlin
    sdk.createPayment(amount, "description", "orderId", userId, extraParams) {
            payment, error ->   //Вызовется после оплаты
    }
```

После вызова в paymentView откроется платежная страница

### Рекурентный платеж:

``` kotlin
   sdk.createRecurringPayment(amount,"description","recurringProfile", "orderId") {
            recurringPayment, error -> //Вызовется после оплаты
   }
```

### Получение статуса платежа:

``` kotlin
   sdk.getPaymentStatus(paymentId) {
            status, error ->  // Вызовется после получения ответа
   }
```

### Клиринг платежа:

``` kotlin
   sdk.makeClearingPayment(paymentId, amount) {     // Если указать null вместо суммы клиринга, то клиринг пройдет на всю сумму платежа
            capture, error -> // Вызовется после клиринга
   }
```

### Отмена платежа:

``` kotlin
   sdk.makeCancelPayment(paymentId) {
            payment, error -> //Вызовется после отмены
   }
```

### Возврат платежа:

``` kotlin
   sdk.makeRevokePayment(paymentId, amount) {
            payment, error -> //Вызовется после возврата
   }
```

### Сохранение карты:

``` kotlin
   sdk.addNewCard(userId,"postLink") {
            payment, error -> // Вызовется после добавления
   }
```

- После вызова в paymentView откроется платежная страница

### Получить список сохраненых карт:

``` kotlin
   sdk.getAddedCards(userId){
            cards, error -> // Вызовется после получения ответа
   }
```

### Удаление сохраненой карты:

``` kotlin
   sdk.removeAddedCard(cardId, userId) {
            card, error ->  // Вызовется после получения ответа
   }
```

### Создание платежа сохраненой картой:

``` kotlin
   sdk.createCardPayment(amount, userId, "cardToken", "description", "orderId"){
            payment, error -> // Вызовется после создания
   }
```

> *Внимание: Метод `createCardPayment` с использованием `cardId` является устаревшим.*

### Для оплаты созданного платежа:

``` kotlin
   sdk.payByCard(paymentId){
            payment, error -> //Вызовется после оплаты
   }
```

- После вызова в paymentView откроется платежная страница для 3ds аутентификации

### Для оплаты созданного платежа c безакцепным списанием:

``` kotlin
   sdk.createNonAcceptancePayment(paymentId, merchantId){
            payment, error -> //Вызовется после оплаты
   }
```

# Интеграция Google Pay

### 1. Добавьте в ваш `build.gradle:`

``` groovy
   implementation 'com.google.android.gms:play-services-wallet:19.2.1'
```

### 2. Далее следует обновить `AndroidManifest:`

 ``` manifest
    <application>
    ...
    <!-- Enables the Google Pay API -->
    <meta-data
        android:name="com.google.android.gms.wallet.api.enabled"
        android:value="true" />
    </application> 
 	
```

### 3. Добавьте PayButton в ваше xml:

 ``` xml
 <com.google.android.gms.wallet.button.PayButton
        android:id="@+id/buttonPaymentByGoogle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />
```

### 4. Инициализация кнопки для оплаты с Google Pay `PayButton`:

``` kotlin 
      val googlePayButton: PayButton = findViewById(R.id.buttonPaymentByGoogle)
         googlePayButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonType(ButtonType.CHECKOUT)
                .setButtonTheme(ButtonTheme.LIGHT)
                .build()
        )
```

### 5. Инициализация `PaymentsClient:`

``` kotlin
	 val googlePaymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                .setTheme(WalletConstants.THEME_LIGHT)
                .build()
        )
```

- Подробнее: `Wallet.getPaymentsClient`- метод, для получения экземпляра PaymentsClient.

### Параметры методов

| Параметр                 | Значение       |
|--------------------------|----------------|
| `ENVIRONMENT_PRODUCTION` | боевая среда   |
| `ENVIRONMENT_TEST`       | тестовая среда |
| `THEME_LIGHT`            | светлая тема   |

### 6. Создание платежа с использованием Google Pay:

```  kotlin
     // Повесим на кнопку обработчик нажатий
     googlePayButton.setOnClickListener {
     
     // Создание платежа с использованием Google Pay
     sdk.createGooglePayment(amount, description, orderId, userId, extraParams) 
     { payment, error -> // Вызывается после создания
     
       // Получаем url для подтверждения платежа
       url = payment?.redirectUrl.toString()
       
       // Инициирует загрузку данных платежа с использованием Google Pay
       AutoResolveHelper.resolveTask<PaymentData>(
                    googlePaymentsClient.loadPaymentData(createPaymentDataRequest()),
                    this,
                    REQUEST_CODE
                )
         }
      }
     
       companion object {
        // Код запроса, который будет использоваться при вызове
             const val REQUEST_CODE = 123
       }
	
```

- Подробнее:

    - `sdk.createGooglePayment`— cоздание платежа с использованием Google Pay

    - `createPaymentDataRequest()` — метод, который возвращает объект PaymentDataRequest. Этот объект
      определяет параметры и требования для запроса данных платежа, таких как методы оплаты, адрес
      доставки и др. (ниже приведен код)

    - `loadPaymentData()` — инициирует асинхронную задачу для загрузки данных платежа с использованием
      предоставленного запроса.

    - `AutoResolveHelper.resolveTask<PaymentData>()`— метод, используется для обработки задачи
      загрузки данных платежа.

    - `REQUEST_CODE`— код запроса, который будет использоваться для идентификации результата задачи в
      методе onActivityResult.


### 7. Метод `createPaymentDataRequest()` — создает и возвращает запрос `PaymentDataRequest` для использования с Google Pay API:

``` kotlin
     private fun createPaymentDataRequest(): PaymentDataRequest {
     
    // Создание нового строителя запроса
    val request = PaymentDataRequest.newBuilder()
    
        // Установка информации о транзакции
        .setTransactionInfo(
            TransactionInfo.newBuilder()
            
                // Статус окончательной цены
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                
                // Общая сумма платежа
                .setTotalPrice("12.00")
                
                // Код валюты (например, "KZT")
                .setCurrencyCode("KZT")
                
                .build()
        )
        
        // Разрешение оплаты банковской картой
        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
        
        // Разрешение оплаты токенизированной банковской картой
        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
        
        // Установка требований к банковской карте
        .setCardRequirements(
            CardRequirements.newBuilder()
                // Разрешенные сети карт
                .addAllowedCardNetworks(
                    Arrays.asList(
                        WalletConstants.CARD_NETWORK_VISA,
                        WalletConstants.CARD_NETWORK_MASTERCARD
                    )
                )
                .build()
        )
    
    // Настройка параметров токенизации метода оплаты
    val params = PaymentMethodTokenizationParameters.newBuilder()
    
        // Тип токенизации (в данном случае - платежный шлюз)
        .setPaymentMethodTokenizationType(
            WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY
        )
        
        // Параметры для платежного шлюза (например, "gateway", "gatewayMerchantId")
        .addParameter("gateway", "yourGateway")
        .addParameter("gatewayMerchantId", "yourMerchantIdGivenFromYourGateway")
        .build()
    
    // Установка параметров токенизации в запрос
    request.setPaymentMethodTokenizationParameters(params)
    
    // Возвращение построенного запроса
    return request.build()
   }
	
```

- Подробнее:
    - `PaymentDataRequest.newBuilder()` - создает новый строитель запроса данных платежа.

    - `setTransactionInfo()`- устанавливает информацию о транзакции, такую как общая сумма платежа и
      код валюты.

    - `PaymentMethodTokenizationParameters.newBuilder()` - создает строитель параметров токенизации
      метода оплаты.

    - `setPaymentMethodTokenizationType()` - устанавливает тип токенизации (в данном случае -
      платежный
      шлюз).

    - `addParameter()`- добавляет параметры для платежного шлюза, такие как "gateway" и
      "gatewayMerchantId".

    - `setPaymentMethodTokenizationParameters()`- устанавливает параметры токенизации в запрос
      данных платежа.

### 8. Получаем результат в onActivityResult():

``` kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                       if (data == null)
                            return
                        val paymentData = PaymentData.getFromIntent(data)
                        val token = paymentData?.paymentMethodToken?.token ?: return
                        
                        // После получения токена мы подтверждаем платеж, отправляя запрос на ранее полученный URL. 
                        // Описано в пункте №9               
                        sdk.confirmGooglePayment(url, token) { payment, error ->
                        }
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        if (data == null)
                            return
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                      
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }
```

- Подробнее:
    - `onActivityResult` - используется для обработки результатов, возвращаемых активностью
      интеграции с Google Pay.

    - `data` - объект , содержащий данные, возвращенные активностью.

    - `Activity.RESULT_OK` - это константа, указывает на успешное завершение операции

    - `AutoResolveHelper.RESULT_ERROR` - это константа,используемая для указания, что произошла ошибка
      при разрешении запроса.


### 9. Подтверждение платежа:

``` kotlin
 sdk.confirmGooglePayment(url, token) { payment, error -> }

```