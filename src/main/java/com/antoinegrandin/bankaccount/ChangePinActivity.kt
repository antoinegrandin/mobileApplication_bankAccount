package com.antoinegrandin.bankaccount

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Matcher
import java.util.regex.Pattern


class ChangePinActivity : AppCompatActivity() {
    private val SHARED_PREFS = "sharedPrefs"
    private val PASSWORD = "password"
    private val ALREADY_CONNECT = "already_connect"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        val buttonSave: Button = findViewById(R.id.button_validation_password)
        val textViewChangePassword: EditText = findViewById(R.id.editText_new_password)
        val textViewConfirmPassword: EditText = findViewById(R.id.editText_new_password_confirmation)

        buttonSave.setOnClickListener {
            if(textViewChangePassword.text.toString() == textViewConfirmPassword.text.toString()){
                if (isValidPassword(textViewChangePassword.text.toString().trim())) {
                    val sha256Input: ByteArray = textViewChangePassword.text.toString().toByteArray()
                    val sha256Data = BigInteger(1, encryptSha256(sha256Input))

                    var sha256Str: String = sha256Data.toString(16)

                    if (sha256Str.length < 32) {
                        sha256Str = "0$sha256Str"
                    }

                    val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString(PASSWORD, sha256Str)
                    editor.putBoolean(ALREADY_CONNECT, true)
                    editor.apply()
                    finish()
                    Toast.makeText(this, "Your password has been correctly updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid password (1 lowercase, 1 uppercase, 1 digit and minimum 4 characters)", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(
                    this,
                    "Your password and its confirmation are different",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$"
        val pattern: Pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    @Throws(java.lang.Exception::class)
    fun encryptSha256(data: ByteArray): ByteArray? {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        sha256.update(data)
        return sha256.digest()
    }
}