package com.uhamka.bosm.sign.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.uhamka.bosm.R
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_sign_up.btn_continue
import kotlinx.android.synthetic.main.activity_sign_up_contact.*

class SignUpContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_contact)

        val preferences = Preferences(this)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        val name = "Hai, " + preferences.getValues("nameLogin")
        tv_name.text = name

        btn_continue.setOnClickListener {
            val phone = et_phone.text.toString()
            val whatsapp = et_whatsapp.text.toString()
            val email = et_email.text.toString()

            if (phone.equals("")){
                et_phone.error = "Lengkapi isian terlebih dahulu!"
                et_phone.requestFocus()
            } else if (whatsapp.equals("")){
                et_whatsapp.error = "Lengkapi isian terlebih dahulu!"
                et_whatsapp.requestFocus()
            } else if (email.equals("")){
                et_email.error = "Lengkapi isian terlebih dahulu!"
                et_email.requestFocus()
            } else {
                preferences.setValues("phone", phone)
                preferences.setValues("whatsapp", whatsapp)
                preferences.setValues("email", email)
                startActivity(Intent(this, SignUpPhotoActivity::class.java))
            }
        }

        btn_skip.setOnClickListener {
            startActivity(Intent(this, SignUpPhotoActivity::class.java))
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Tekan tombol skip untuk mengatur kontak Anda nanti", Toast.LENGTH_LONG).show()
    }
}