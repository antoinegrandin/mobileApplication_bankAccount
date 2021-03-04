package com.antoinegrandin.bankaccount

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.math.BigInteger
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    private val SHARED_PREFS = "sharedPrefs"
    private val PASSWORD = "password"
    private val ALREADY_CONNECT = "already_connect"

    private var password: String? = null
    private var already_connect = false
    private val defaultPassword = "123"

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get()=
            @RequiresApi(Build.VERSION_CODES.P)
            object: BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if(errorCode == 10){
                        notifyUser("Authentication cancelled")
                    } else {
                        notifyUser("Authentication error: $errString")
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication success!")
                    startActivity(Intent(this@MainActivity, AccountActivity::class.java))
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.floatingActionButton_resetData).setOnClickListener{
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(PASSWORD, "123")
            editor.putBoolean(ALREADY_CONNECT, false)
            editor.apply()
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStart() {
        super.onStart()
        loadData()

        val passwordTextView: TextView = findViewById(R.id.textView_password)

        findViewById<Button>(R.id.button_validation_id).setOnClickListener{
            if(already_connect){
                val sha256Input: ByteArray = passwordTextView.text.toString().toByteArray()

                val sha256Data = BigInteger(1, encryptSha256(sha256Input))

                var sha256Str: String = sha256Data.toString(16)

                if (sha256Str.length < 32) {
                    sha256Str = "0$sha256Str"
                }

                if (sha256Str == password) {
                    passwordTextView.text = ""
                    val accountIntent = Intent(this, AccountActivity::class.java)
                    startActivity(accountIntent)
                } else {
                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG).show()
                }
            } else {
                if(passwordTextView.text.toString() == password){
                    passwordTextView.text = ""
                    val changePinActivity = Intent(this, ChangePinActivity::class.java)
                    startActivityForResult(changePinActivity, 1)
                } else {
                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG).show()
                }
            }
        }

        if(already_connect){
            checkBiometricSupport()
        } else {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("First connection to your bank app")
            builder.setMessage("Your temporary default password is '123'.")
            builder.setPositiveButton("OK") { _, _ -> }
            builder.show()
        }

        findViewById<AppCompatImageView>(R.id.fingerprint_ImageView).setOnClickListener{
            if(already_connect){
                startFingerprintPrompt()
            } else {
                notifyUser("Not at the first connection")
            }
        }
    }

    @Throws(java.lang.Exception::class)
    fun encryptSha256(data: ByteArray): ByteArray? {
        val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")
        sha256.update(data)
        return sha256.digest()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        password = sharedPreferences.getString(PASSWORD, defaultPassword)
        already_connect = sharedPreferences.getBoolean(ALREADY_CONNECT, false)
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was cancelled by the user")
        }

        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if(!keyguardManager.isKeyguardSecure) {
            notifyUser("Fingerprint authentication has not been enabled in settings")
            return false
        }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint authentication permission is not enabled")
            return false
        }

        return if(packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        } else true
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startFingerprintPrompt(){
        val biometricPrompt: BiometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Authentication is required")
                .setSubtitle("This app uses fingerprint to keep your data secure")
                .setNegativeButton("Cancel", this.mainExecutor, { _, _ ->
                    notifyUser("Authentication cancelled")
                }).build()

        biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
    }

    private fun notifyUser(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}