package com.antoinegrandin.bankaccount

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountActivity : AppCompatActivity() {

    init {
        System.loadLibrary("api-keys")
    }
    private external fun getKeys() : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bankaccount)

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
            startActivity(Intent(this, AboutActivity::class.java))
        }

        val textViewResult: TextView = findViewById(R.id.text_view_result)
        textViewResult.movementMethod = ScrollingMovementMethod()

        updateAccount(textViewResult)

        findViewById<Button>(R.id.button_refresh).setOnClickListener{
            updateAccount(textViewResult)
        }
    }

    private fun notifyUser(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateAccount(textViewResult: TextView) {
        textViewResult.text = ""
        val db = DataBaseHandler(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(getKeys())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi::class.java)

        val call: Call<List<Account>> = jsonPlaceHolderApi.accounts

        call.enqueue(object : Callback<List<Account>> {
            override fun onResponse(call: Call<List<Account>>, response: Response<List<Account>>) {
                if (!response.isSuccessful) {
                    val errorMessage = "Code : $response.code()"
                    textViewResult.text = errorMessage
                    return
                }

                db.cleanData()

                val accounts: List<Account> = response.body() as List<Account>

                for (account in accounts) {
                    db.insertData(account)

                    var content = ""
                    content += """
                        ID: ${account.id}
                        
                        """.trimIndent()
                    content += """
                        Account Name: ${account.accountName}
                        
                        """.trimIndent()
                    content += """
                        Amount: ${account.amount}
                        
                        """.trimIndent()
                    content += """
                        Iban: ${account.iban}
                        
                        """.trimIndent()
                    content += """
                        Currency: ${account.currency}
                        
                        
                        """.trimIndent()
                    textViewResult.append(content)
                }
            }

            override fun onFailure(call: Call<List<Account>>, throwable: Throwable) {
                val accounts: List<Account> = db.readData()
                val connectionErrorMessage = "No Connection...Display of the latest available data."
                notifyUser(connectionErrorMessage)
                textViewResult.text = ""
                for (account in accounts) {
                    var content = ""
                    content += """
                        ID: ${account.id}
                        
                        """.trimIndent()
                    content += """
                        Account Name: ${account.accountName}
                        
                        """.trimIndent()
                    content += """
                        Amount: ${account.amount}
                        
                        """.trimIndent()
                    content += """
                        Iban: ${account.iban}
                        
                        """.trimIndent()
                    content += """
                        Currency: ${account.currency}
                        
                        
                        """.trimIndent()
                    textViewResult.append(content)
                }
            }
        })
    }
}
