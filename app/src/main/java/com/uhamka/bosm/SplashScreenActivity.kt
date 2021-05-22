@file:Suppress("DEPRECATION")

package com.uhamka.bosm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.uhamka.bosm.sign.signin.SignInActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val handler = Handler()
        handler.postDelayed({
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }, 1000)
    }
}