@file:Suppress("DEPRECATION")

package com.uhamka.bosm.sign.signin

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.InputType
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.model.Users
import com.uhamka.bosm.sign.signup.SignUpActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        preferences = Preferences(this)
        ref = FirebaseDatabase.getInstance().getReference("Users")

        if (preferences.getValues("status").equals("1")) {
            if (preferences.getValues("user").equals("mhs") ||
                    preferences.getValues("user").equals("dosen") ||
                    preferences.getValues("user").equals("admin")) {
                preferences.setValues("tap", "0")
                startActivity(Intent(this, HomeActivity::class.java)
                        .putExtra("from", "login"))
            }
        }

        btn_option.setOnClickListener {
            showOptionUser()
        }

        val choose = "SELECT USER"
        tv_user.text = choose
        checkLogin(choose)

        btn_register.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun isConnected(): Boolean {
        val connectivity = this.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    private fun showOptionUser() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_option_user, null)

        val mhs = view.findViewById<TextView>(R.id.tv_mhs)
        val dosen = view.findViewById<TextView>(R.id.tv_dosen)
        val admin = view.findViewById<TextView>(R.id.tv_admin)

        builder.setView(view)
        val alert = builder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()

        mhs.setOnClickListener {
            alert.dismiss()
            et_id.inputType = InputType.TYPE_CLASS_NUMBER
            val user = "Mahasiswa"
            tv_user.setText(user)
            checkLogin(user)
        }

        dosen.setOnClickListener {
            alert.dismiss()
            et_id.inputType = InputType.TYPE_CLASS_NUMBER
            val user = "Dosen"
            tv_user.setText(user)
            checkLogin(user)
        }

        admin.setOnClickListener {
            alert.dismiss()
            et_id.inputType = InputType.TYPE_CLASS_TEXT
            val user = "Admin"
            tv_user.setText(user)
            checkLogin(user)
        }
    }

    private fun checkLogin(user: String) {
        btn_login.setOnClickListener {
            val id = et_id.text.toString().trim()
            val pass = et_pass.text.toString().trim()

            if (id.equals("")) {
                et_id.error = ("Masukkan ID Anda")
                et_id.requestFocus()
            } else if (pass.equals("")) {
                et_pass.error = ("Masukkan Password Anda")
                et_pass.requestFocus()
            } else {
                if (isConnected()) {
                    preferences.setValues("tap", "0")
                    if (user.equals("Mahasiswa")){
                        mhsLogin(id, pass)
                    } else if (user.equals("Dosen")) {
                        dosenLogin(id, pass)
                    } else if (user.equals("Admin")){
                        adminLogin(id, pass)
                    } else {
                        Toast.makeText(this, "Silakan pilih jenis user terlebih dahulu!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mhsLogin(id: String, pass: String) {
        ref.child("Mahasiswa").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignInActivity, "" + error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mhs = dataSnapshot.getValue(Users::class.java)
                if (mhs == null) {
                    Toast.makeText(this@SignInActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    checkLogin("Mahasiswa")
                    btn_option.setOnClickListener {
                        showOptionUser()
                    }
                } else {
                    if (mhs.password.equals(pass)) {
                        val name = dataSnapshot.child("name").getValue().toString()
                        val prodi = dataSnapshot.child("prodi").getValue().toString()
                        val uri = dataSnapshot.child("photo").getValue().toString()
                        Toast.makeText(this@SignInActivity, "Selamat Datang", Toast.LENGTH_SHORT).show()

                        preferences.setValues("uri", uri)
                        preferences.setValues("idLogin", id)
                        preferences.setValues("nameLogin", name)
                        preferences.setValues("prodiLogin", prodi)
                        preferences.setValues("status", "1")
                        preferences.setValues("user", "mhs")
                        val intent = Intent(this@SignInActivity,
                                HomeActivity::class.java).putExtra("from", "login")
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@SignInActivity, "Password salah!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }

    private fun dosenLogin(id: String, pass: String) {
        ref.child("Dosen").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignInActivity, "" + error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dosen = dataSnapshot.getValue(Users::class.java)
                if (dosen == null) {
                    Toast.makeText(this@SignInActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    checkLogin("Dosen")
                    btn_option.setOnClickListener {
                        showOptionUser()
                    }
                } else {
                    if (dosen.password.equals(pass)) {
                        val uri = dataSnapshot.child("photo").getValue().toString()
                        val name = dataSnapshot.child("name").getValue().toString()
                        Toast.makeText(this@SignInActivity, "Selamat Datang", Toast.LENGTH_SHORT).show()

                        preferences.setValues("uri", uri)
                        preferences.setValues("idLogin", id)
                        preferences.setValues("nameLogin", name)
                        preferences.setValues("status", "1")
                        preferences.setValues("user", "dosen")
                        val intent = Intent(this@SignInActivity,
                                HomeActivity::class.java).putExtra("from", "login")
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@SignInActivity, "Password salah!", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })
    }

    private fun adminLogin(id: String, pass: String) {
        ref.child("Admin").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignInActivity, "" + error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.getValue(Users::class.java)
                if (admin == null) {
                    Toast.makeText(this@SignInActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                    checkLogin("Admin")
                    btn_option.setOnClickListener {
                        showOptionUser()
                    }
                } else {
                    if (admin.password.equals(pass)) {
                        Toast.makeText(this@SignInActivity, "Selamat Datang", Toast.LENGTH_SHORT).show()

                        preferences.setValues("status", "1")
                        preferences.setValues("user", "admin")
                        val intent = Intent(this@SignInActivity,
                                HomeActivity::class.java).putExtra("from", "login")
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@SignInActivity, "Password salah!", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })
    }

    override fun onBackPressed() {
        finishAffinity()
    }

}
