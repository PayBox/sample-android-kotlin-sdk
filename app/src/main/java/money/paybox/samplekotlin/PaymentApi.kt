package money.paybox.samplekotlin


import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

data class PaymentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

interface PaymentApi {
    @POST
    fun sendPaymentRequest(
        @Url url: String,
        @Body body: RequestBody
    ): Call<PaymentResponse>
}

data class RequestBody(
    @SerializedName("type") val type: String,
    @SerializedName("paymentSystem") val paymentSystem: String,
    @SerializedName("token") val token: String
)