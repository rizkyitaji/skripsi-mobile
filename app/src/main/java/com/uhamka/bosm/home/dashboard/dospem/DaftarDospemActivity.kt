@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.dospem

import android.app.AlertDialog
import android.app.Service
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.dospem.te.DosenTEFragment
import com.uhamka.bosm.home.dashboard.dospem.ti.DosenTIFragment
import com.uhamka.bosm.home.dashboard.dospem.tm.DosenTMFragment
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_daftar_dospem.*

class DaftarDospemActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_dospem)

        preferences = Preferences(this)

        val fragmentInformatics = DosenTIFragment()
        val fragmentElectro = DosenTEFragment()
        val fragmentMachine = DosenTMFragment()

        tabs_main.addTab(tabs_main.newTab().setText("TI"))
        tabs_main.addTab(tabs_main.newTab().setText("TE"))
        tabs_main.addTab(tabs_main.newTab().setText("TM"))

        setFragment(fragmentInformatics)

        tabs_main.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> setFragment(fragmentInformatics)
                    1 -> setFragment(fragmentElectro)
                    else -> setFragment(fragmentMachine)
                }
            }
        })

        if (preferences.getValues("user").equals("admin")) {
            btn_fab.visibility = View.VISIBLE
        } else {
            btn_fab.visibility = View.GONE
        }

        btn_fab.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = layoutInflater.inflate(R.layout.layout_add_dosen, null)
            dialog.setContentView(view)
            dialog.show()

            val close = view.findViewById<ImageView>(R.id.iv_close)
            dialog.setCancelable(false)
            close.setOnClickListener {
                dialog.dismiss()
            }

            val submit = view.findViewById<Button>(R.id.btn_submit)
            val textProdi = view.findViewById<TextView>(R.id.tv_prodi)
            val btnProdi = view.findViewById<ConstraintLayout>(R.id.btn_prodi)

            val editName = view.findViewById<EditText>(R.id.et_name)
            val editNIDN = view.findViewById<EditText>(R.id.et_nidn)
            val editExp = view.findViewById<EditText>(R.id.et_expertise)

            btnProdi.setOnClickListener {
                optionProdi(textProdi)
            }

            submit.setOnClickListener {
                val name = editName.text.toString()
                val nidn = editNIDN.text.toString()
                val prodi = textProdi.text.toString()
                val expertise = editExp.text.toString()

                if (prodi.equals("Choose")) {
                    Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
                } else if (name.equals("")) {
                    editName.error = "Lengkapi isian terlebih dahulu!"
                    editName.requestFocus()
                } else if (nidn.equals("")) {
                    editNIDN.error = "Lengkapi isian terlebih dahulu!"
                    editNIDN.requestFocus()
                } else if (expertise.equals("")) {
                    editExp.error = "Lengkapi isian terlebih dahulu!"
                    editExp.requestFocus()
                } else {
                    if (isConnected()) {
                        val ref = FirebaseDatabase.getInstance()
                                .getReference("Pembimbing").child(prodi).child(nidn)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                ref.child("nidn").setValue(nidn)
                                ref.child("name").setValue(name)
                                ref.child("expertise").setValue(expertise)

                                Toast.makeText(this@DaftarDospemActivity, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DaftarDospemActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun optionProdi(mText: TextView) {
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
            mText.text = prodi
        }

        te.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Elektro"
            mText.text = prodi
        }

        tm.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Mesin"
            mText.text = prodi
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.layout_frame, fragment)
                .commit()
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
        preferences.setValues("tap", "0")
        super.onBackPressed()
    }
}