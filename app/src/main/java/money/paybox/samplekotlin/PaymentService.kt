package money.paybox.samplekotlin

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PaymentService {

    fun sendPaymentRequest(url: String, body: RequestBody) {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://customer.freedompay.money")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        val api = retrofit.create(PaymentApi::class.java)

        val call = api.sendPaymentRequest(url, body)

        call.enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                if (response.isSuccessful) {
                    val paymentResponse = response.body()
                    println("Success: ${paymentResponse?.status}, ${paymentResponse?.message}")
                } else {
                    println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                println("Failure: ${t.message}")
            }
        })
    }
}