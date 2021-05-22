@file:Suppress("DEPRECATION", "DEPRECATION")

package com.uhamka.bosm.sign.signup

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Users
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        preferences = Preferences(this)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_user.setOnClickListener {
            showOptionUser()
        }

        btn_prodi.setOnClickListener {
            optionProdi()
        }

        val user = tv_user.text.toString()
        checkData(user)
    }

    private fun checkData(user: String) {
        if (user.equals("Mahasiswa")) {
            prodi.visibility = View.VISIBLE
            btn_prodi.visibility = View.VISIBLE
            et_frontId.visibility = View.VISIBLE
            tv_strip_one.visibility = View.VISIBLE
            tv_midId.visibility = View.VISIBLE
            tv_strip_two.visibility = View.VISIBLE
            tv_midId.text = getString(R.string._03015)
            preferences.setValues("user", "mhs")
            tv_id.text = getString(R.string.id_nim)
        } else if (user.equals("Dosen")) {
            prodi.visibility = View.GONE
            btn_prodi.visibility = View.GONE
            et_frontId.visibility = View.GONE
            tv_strip_one.visibility = View.GONE
            tv_midId.visibility = View.GONE
            tv_strip_two.visibility = View.GONE
            preferences.setValues("user", "dosen")
            tv_id.text = getString(R.string.id_nidn)
        } else {
            prodi.visibility = View.GONE
            btn_prodi.visibility = View.GONE
            et_frontId.visibility = View.GONE
            tv_strip_one.visibility = View.GONE
            tv_midId.visibility = View.GONE
            tv_strip_two.visibility = View.GONE
            tv_id.text = getString(R.string.id)
        }

        btn_continue.setOnClickListener {
            val name = et_name.text.toString()
            val prodi = tv_prodi.text.toString()
            val pass = et_password.text.toString()
            val frontId = et_frontId.text.toString()
            val backId = et_backId.text.toString()

            if (user.equals("Choose")) {
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (name.equals("")) {
                et_name.error = "Lengkapi isian terlebih dahulu!"
                et_name.requestFocus()
            } else if (backId.equals("")) {
                et_backId.error = "Lengkapi isian terlebih dahulu!"
                et_backId.requestFocus()
            } else if (pass.equals("")) {
                et_password.error = "Lengkapi isian terlebih dahulu!"
                et_password.requestFocus()
            } else if (user.equals("Mahasiswa")){
                if (frontId.equals("")){
                    et_frontId.error = "Lengkapi isian terlebih dahulu!"
                    et_frontId.requestFocus()
                } else if (prodi.equals("Choose")){
                    Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
                } else {
                    if (isConnected()) {
                        val id = frontId + "03015" + backId
                        val ref = FirebaseDatabase.getInstance()
                                .getReference("Users").child("Mahasiswa").child(id)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val users = snapshot.getValue(Users::class.java)
                                if (users == null) {
                                    preferences.setValues("pass", pass)
                                    preferences.setValues("idLogin", id)
                                    preferences.setValues("nameLogin", name)
                                    preferences.setValues("prodiLogin", prodi)
                                    startActivity(Intent(this@SignUpActivity, SignUpContactActivity::class.java))
                                } else {
                                    Toast.makeText(this@SignUpActivity, "ID " + id + " sudah ada!", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@SignUpActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val ref = FirebaseDatabase.getInstance()
                        .getReference("Users").child("Dosen").child(backId)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.getValue(Users::class.java)
                        if (users == null) {
                            preferences.setValues("pass", pass)
                            preferences.setValues("idLogin", backId)
                            preferences.setValues("nameLogin", name)
                            startActivity(Intent(this@SignUpActivity, SignUpContactActivity::class.java))
                        } else {
                            Toast.makeText(this@SignUpActivity, "ID " + backId + " sudah ada!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SignUpActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun showOptionUser() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_user_type, null)

        val mhs = view.findViewById<TextView>(R.id.tv_mhs)
        val dosen = view.findViewById<TextView>(R.id.tv_dosen)

        builder.setView(view)
        val alert = builder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()

        mhs.setOnClickListener {
            alert.dismiss()
            val user = "Mahasiswa"
            tv_user.text = user
            checkData(user)
        }

        dosen.setOnClickListener {
            alert.dismiss()
            val user = "Dosen"
            tv_user.text = user
            checkData(user)
        }
    }

    private fun optionProdi() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_program_studi, null)
        val ti = view.findViewById<TextView>(R.id.tv_ti)
        val te = view.findViewById<TextView>(R.id.tv_te)
        val tm = view.findViewById<TextView>(R.id.tv_tm)

        builder.setView(view)
        val alert = builder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()

        ti.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Informatika"
            tv_prodi.text = prodi
        }

        te.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Elektro"
            tv_prodi.text = prodi
        }

        tm.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Mesin"
            tv_prodi.text = prodi
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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}