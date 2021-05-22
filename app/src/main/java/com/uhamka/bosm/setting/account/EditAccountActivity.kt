@file:Suppress("DEPRECATION")

package com.uhamka.bosm.setting.account

import android.app.Service
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_edit_account.*

class EditAccountActivity : AppCompatActivity() {

    lateinit var mText: String
    lateinit var current: String
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)

        preferences = Preferences(this)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        val edit = intent.getStringExtra("edit")!!
        val id = preferences.getValues("idLogin").toString()

        if (preferences.getValues("user").equals("mhs")) {
            ref = FirebaseDatabase.getInstance().getReference("Users").child("Mahasiswa").child(id)
        } else {
            ref = FirebaseDatabase.getInstance().getReference("Users").child("Dosen").child(id)
        }

        if (edit.equals("password")) {
            editPassword()
        } else {
            editContact(edit)
        }
    }

    private fun editPassword() {
        textCurrent.text = getString(R.string.current_password)
        tv_current.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)

        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val current = snapshot.child("password").value.toString()
                    tv_current.text = current
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditAccountActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

        linear_contact.visibility = View.GONE
        textNew.text = getString(R.string.new_password)
        til_new.isPasswordVisibilityToggleEnabled = true

        btn_save.setOnClickListener {
            if (isConnected()) {
                val new = et_new.text.toString()
                val pass = et_pass.text.toString()

                if (new.equals("")) {
                    et_new.error = "Silakan ketik sandi baru Anda!"
                    et_new.requestFocus()
                } else if (pass.equals("")) {
                    et_pass.error = "Silakan ketik ulang sandi Anda!"
                    et_pass.requestFocus()
                } else if (!new.equals(pass)) {
                    et_pass.error = "Password Anda tidak sama! Silakan ketik ulang sandi Anda!"
                    et_pass.requestFocus()
                } else {
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("password").setValue(pass)
                            Toast.makeText(this@EditAccountActivity, "Password updated", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@EditAccountActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editContact(path: String) {
        if (path.equals("phone")) {
            mText = "Phone Number"
            textCurrent.text = getString(R.string.current_phone_number)
            current = getString(R.string.current_phone_number_is_not_set)
            tv_current.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0)
            textNew.text = getString(R.string.new_phone_number)
            tv_icon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0)
            et_contact.setHint(mText)
            til_new.visibility = View.GONE
        } else if (path.equals("whatsapp")) {
            mText = "Whatsapp Number"
            textCurrent.text = getString(R.string.current_whatsapp_number)
            current = getString(R.string.current_whatsapp_number_is_not_set)
            tv_current.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp_dark, 0, 0, 0)
            textNew.text = getString(R.string.new_whatsapp_number)
            tv_icon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_whatsapp_dark, 0, 0, 0)
            et_contact.setHint(mText)
            til_new.visibility = View.GONE
        } else {
            mText = "Email Address"
            textCurrent.text = getString(R.string.current_email_address)
            current = getString(R.string.current_email_address_is_not_set)
            tv_current.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0)
            textNew.text = getString(R.string.new_email_address)
            linear_contact.visibility = View.GONE
            til_new.isPasswordVisibilityToggleEnabled = false
            et_new.setHint(mText)
            et_new.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            et_new.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0)
        }

        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.child(path).value.toString()
                    if (value.equals("null")) {
                        tv_current.text = current
                    } else {
                        if (path.equals("whatsapp")) {
                            val wa = "0" + value
                            tv_current.text = wa
                        } else {
                            tv_current.text = value
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditAccountActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

        textPass.visibility = View.INVISIBLE
        til_pass.visibility = View.INVISIBLE

        btn_save.setOnClickListener {
            if (isConnected()) {
                val new = et_new.text.toString()
                val contact = et_contact.text.toString()

                if (path.equals("email")) {
                    if (new.equals("")) {
                        et_new.error = "Lengkapi isian Anda terlebih dahulu!"
                        et_new.requestFocus()
                    } else {
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                ref.child(path).setValue(new)
                                Toast.makeText(this@EditAccountActivity, mText + " Updated", Toast.LENGTH_SHORT).show()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditAccountActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    if (contact.equals("")) {
                        et_contact.error = "Lengkapi isian Anda terlebih dahulu!"
                        et_contact.requestFocus()
                    } else {
                        val current = "0" + contact
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (path.equals("phone")) {
                                    ref.child(path).setValue(current)
                                } else {
                                    ref.child(path).setValue(contact)
                                }
                                Toast.makeText(this@EditAccountActivity, mText + " Updated", Toast.LENGTH_SHORT).show()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@EditAccountActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
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