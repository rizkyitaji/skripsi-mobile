package com.uhamka.bosm.home.dashboard.user

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_detail_user.*

class DetailUserActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var name: String
    lateinit var pass: String
    lateinit var list: String
    lateinit var prodi: String
    lateinit var photo: String
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_user)

        preferences = Preferences(this)
        id = preferences.getValues("id").toString()
        list = intent.getStringExtra("from")!!

        if (list.equals("listMHS")) {
            space3.visibility = View.VISIBLE
            tv_prodi.visibility = View.VISIBLE
            textProdi.visibility = View.VISIBLE
            textId.text = getString(R.string.npm)
            ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Mahasiswa").child(id)
        } else if (list.equals("listDosen")) {
            space3.visibility = View.GONE
            tv_prodi.visibility = View.GONE
            textProdi.visibility = View.GONE
            textId.text = getString(R.string.nidn)
            ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Dosen").child(id)
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                name = snapshot.child("name").value.toString()
                prodi = snapshot.child("prodi").value.toString()
                pass = snapshot.child("password").value.toString()
                val phone = snapshot.child("phone").value.toString()
                val email = snapshot.child("email").value.toString()
                val whatsapp = snapshot.child("whatsapp").value.toString()
                val setWA = "0" + whatsapp

                tv_id.text = id
                tv_name.text = name
                tv_prodi.text = prodi
                tv_password.text = pass
                if (phone.equals("null")) {
                    tv_phone.text = getString(R.string.strip)
                } else if (email.equals("null")) {
                    tv_email.text = getString(R.string.strip)
                } else if (whatsapp.equals("null")) {
                    tv_whatsapp.text = getString(R.string.strip)
                } else {
                    tv_phone.text = phone
                    tv_email.text = email
                    tv_whatsapp.text = setWA
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailUserActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        val user = FirebaseDatabase.getInstance()
                .getReference("Users").child("Mahasiswa").child(id)
        user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                photo = snapshot.child("photo").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailUserActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        btn_edit.setOnClickListener {
            editData()
        }

        btn_view.setOnClickListener {
            preferences.setValues("id", id)
            preferences.setValues("name", name)
            preferences.setValues("prodi", prodi)
            preferences.setValues("photo", photo)
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra("from", list))
        }
    }

    private fun editData() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_edit_user, null)
        dialog.setContentView(view)
        dialog.show()

        val close = view.findViewById<ImageView>(R.id.iv_close)
        dialog.setCancelable(false)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val space = view.findViewById<TextView>(R.id.space3)
        val textID = view.findViewById<TextView>(R.id.textId)
        val textProdi = view.findViewById<TextView>(R.id.textProdi)

        val editID = view.findViewById<EditText>(R.id.et_id)
        val editName = view.findViewById<EditText>(R.id.et_name)
        val editPass = view.findViewById<EditText>(R.id.et_pass)
        val editProdi = view.findViewById<TextView>(R.id.tv_prodi)
        val btnProdi = view.findViewById<ConstraintLayout>(R.id.btn_prodi)

        if (list.equals("listMHS")) {
            space.visibility = View.VISIBLE
            textProdi.visibility = View.VISIBLE
            btnProdi.visibility = View.VISIBLE
            textID.text = getString(R.string.npm)
        } else if (list.equals("listDosen")) {
            space.visibility = View.GONE
            textProdi.visibility = View.GONE
            btnProdi.visibility = View.GONE
            textID.text = getString(R.string.nidn)
        }

        editID.setText(id)
        editName.setText(name)
        editPass.setText(pass)
        editProdi.setText(prodi)

        btnProdi.setOnClickListener {
            optionProdi(editProdi)
        }

        val submit = view.findViewById<Button>(R.id.btn_submit)
        submit.setOnClickListener {
            val getID = editID.text.toString()
            val getName = editName.text.toString()
            val getPass = editPass.text.toString()
            val getProdi = editProdi.text.toString()

            if (getProdi.equals("Choose")){
                Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (getName.equals("")){
                editName.error = "Lengkapi isian terlebih dahulu!"
                editName.requestFocus()
            } else if (getID.equals("")){
                editID.error = "Lengkapi isian terlebih dahulu!"
                editID.requestFocus()
            } else if (getPass.equals("")){
                editPass.error = "Lengkapi isian terlebih dahulu!"
                editPass.requestFocus()
            } else {
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        ref.child("id").setValue(getID)
                        ref.child("name").setValue(getName)
                        ref.child("prodi").setValue(getProdi)
                        ref.child("password").setValue(getPass)

                        Toast.makeText(this@DetailUserActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailUserActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun optionProdi(tv_prodi: TextView) {
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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}