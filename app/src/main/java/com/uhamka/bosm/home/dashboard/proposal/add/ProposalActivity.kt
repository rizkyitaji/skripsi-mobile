@file:Suppress("DEPRECATION", "DEPRECATION")

package com.uhamka.bosm.home.dashboard.proposal.add

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.dashboard.adapter.OptionDosenAdapter
import com.uhamka.bosm.home.repository.RepositoryActivity
import com.uhamka.bosm.model.Pembimbing
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_proposal.*

class ProposalActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var name: String
    lateinit var nim: String
    lateinit var prodi: String
    lateinit var dosen: String
    lateinit var title: String
    lateinit var desc: String
    lateinit var setProdi: String
    lateinit var preferences: Preferences
    private var dataList = ArrayList<Pembimbing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposal)

        preferences = Preferences(this)
        preferences.setValues("tap", "0")

        if (preferences.getValues("action").equals("add")) {
            val push = FirebaseDatabase.getInstance()
                    .getReference("Proposal").push()
            id = push.key.toString()
            tv_title.text = getString(R.string.proposal)
            setProdi = preferences.getValues("prodiLogin").toString()
            et_name.setText(preferences.getValues("nameLogin").toString())
            et_nim.setText(preferences.getValues("idLogin").toString())
            tv_prodi.text = setProdi
            optionDosen(setProdi)
        } else {
            tv_title.text = getString(R.string.edit)
            id = preferences.getValues("id").toString()
            val ref = FirebaseDatabase.getInstance()
                    .getReference("Proposal").child(id)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    setProdi = snapshot.child("prodi").value.toString()
                    val setDosen = snapshot.child("pembimbing").value.toString()

                    tv_prodi.text = setProdi
                    tv_pembimbing.text = setDosen
                    et_name.setText(snapshot.child("name").value.toString())
                    et_nim.setText(snapshot.child("nim").value.toString())
                    et_judul.setText(snapshot.child("judul").value.toString())
                    et_desc.setText(snapshot.child("desc").value.toString())

                    preferences.setValues("dosen", setDosen)
                    if (preferences.getValues("action").equals("edit")) {
                        if (preferences.getValues("user").equals("admin")) {
                            optionDosen(setProdi)
                        } else {
                            btn_dospem.setOnClickListener {
                                Toast.makeText(this@ProposalActivity,
                                        "Silakan hubungi Kaprodi untuk mengganti dosen pembimbing Anda", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        optionDosen(setProdi)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }


        btn_submit.setOnClickListener {
            name = et_name.text.toString()
            nim = et_nim.text.toString()
            prodi = tv_prodi.text.toString()
            dosen = tv_pembimbing.text.toString()
            title = et_judul.text.toString()
            desc = et_desc.text.toString().trim()

            if (name.equals("")) {
                et_name.error = "Lengkapi isian terlebih dahulu!"
                et_name.requestFocus()
            } else if (nim.equals("")) {
                et_nim.error = "Lengkapi isian terlebih dahulu!"
                et_nim.requestFocus()
            } else if (title.equals("")) {
                et_judul.error = "Lengkapi isian terlebih dahulu!"
                et_judul.requestFocus()
            } else if (desc.equals("")) {
                et_desc.error = "Lengkapi isian terlebih dahulu!"
                et_desc.requestFocus()
            } else if (prodi.equals("Choose")) {
                dataNull()
            } else if (dosen.equals("Choose")) {
                dataNull()
            } else {
                if (isConnected()) {
                    saveData()
                } else {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_prodi.setOnClickListener {
            optionProdi()
        }
    }

    private fun saveData() {
        val nidn = preferences.getValues("nidn").toString()

        if (preferences.getValues("action").equals("edit")) {
            saveProposal(nidn)
            val repository = FirebaseDatabase.getInstance()
                    .getReference("Repository").child(prodi).child(nim)
            repository.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    repository.child("id").setValue(id)
                    repository.child("name").setValue(name)
                    repository.child("nim").setValue(nim)
                    repository.child("prodi").setValue(prodi)
                    repository.child("nidn").setValue(nidn)
                    repository.child("pembimbing").setValue(dosen)
                    repository.child("judul").setValue(title)
                    repository.child("desc").setValue(desc)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_LONG).show()
                }
            })
        } else if (preferences.getValues("action").equals("modify")) {
            saveProposal(nidn)
            val skripsi = FirebaseDatabase.getInstance()
                    .getReference("Skripsi").child(prodi).child(nim)
            skripsi.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val getDosen = preferences.getValues("dosen").toString()
                    if (!dosen.equals(getDosen)){
                        skripsi.removeValue()
                    } else {
                        skripsi.child("id").setValue(id)
                        skripsi.child("name").setValue(name)
                        skripsi.child("nim").setValue(nim)
                        skripsi.child("prodi").setValue(prodi)
                        skripsi.child("nidn").setValue(nidn)
                        skripsi.child("pembimbing").setValue(dosen)
                        skripsi.child("judul").setValue(title)
                        skripsi.child("desc").setValue(desc)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_LONG).show()
                }
            })
        } else {
            saveProposal(nidn)
        }

        if (preferences.getValues("action").equals("add")) {
            preferences.setValues("id", id)
            preferences.setValues("nim", nim)
            preferences.setValues("prodi", prodi)
            startActivity(Intent(this, AddSuccessActivity::class.java)
                    .putExtra("notif", "add"))
        } else {
            if (preferences.getValues("user").equals("mhs")) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, RepositoryActivity::class.java))
            }
            Toast.makeText(this, "Proposal telah diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProposal(nidn: String) {
        val proposal = FirebaseDatabase.getInstance()
                .getReference("Proposal").child(id)
        proposal.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                proposal.child("id").setValue(id)
                proposal.child("name").setValue(name)
                proposal.child("nim").setValue(nim)
                proposal.child("prodi").setValue(prodi)
                proposal.child("nidn").setValue(nidn)
                proposal.child("pembimbing").setValue(dosen)
                proposal.child("judul").setValue(title)
                proposal.child("desc").setValue(desc)
                if (!preferences.getValues("action").equals("edit")) {
                    val getDosen = preferences.getValues("dosen").toString()
                    if (!dosen.equals(getDosen) || preferences.getValues("action").equals("add")){
                        proposal.child("nidn_fk").setValue(nidn)
                    }
                    proposal.child("status").setValue("Menunggu Disetujui")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_LONG).show()
            }
        })

        val status = FirebaseDatabase.getInstance()
                .getReference("Status").child(nim).child(id)
        status.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                status.child("id").setValue(id)
                status.child("nim").setValue(nim)
                if (!preferences.getValues("action").equals("edit")) {
                    status.child("status").setValue("Menunggu Disetujui")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun dataNull() {
        Toast.makeText(this, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_LONG).show()
    }

    private fun optionProdi() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_program_studi, null)
        val ti = view.findViewById<TextView>(R.id.tv_ti)
        val te = view.findViewById<TextView>(R.id.tv_te)
        val tm = view.findViewById<TextView>(R.id.tv_tm)
        val choose = view.findViewById<TextView>(R.id.tv_choose)
        val text = "Choose"

        builder.setView(view)
        val alert = builder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()

        choose.setOnClickListener {
            alert.dismiss()
            tv_prodi.text = text
            optionDosen(text)
        }

        ti.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Informatika"
            tv_prodi.text = prodi
            tv_pembimbing.text = text
            optionDosen(prodi)
        }

        te.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Elektro"
            tv_prodi.text = prodi
            tv_pembimbing.text = text
            optionDosen(prodi)
        }

        tm.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Mesin"
            tv_prodi.text = prodi
            tv_pembimbing.text = text
            optionDosen(prodi)
        }
    }

    private fun optionDosen(prodi: String) {
        btn_dospem.setOnClickListener {
            if (prodi.equals("Choose")) {
                Toast.makeText(this, "Silakan pilih program studi terlebih dahulu!", Toast.LENGTH_LONG).show()
            } else {
                val builder = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.layout_dosen, null)
                builder.setView(view)
                val alert = builder.create()
                alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alert.show()

                val choose = view.findViewById<TextView>(R.id.tv_choose)
                choose.setOnClickListener {
                    alert.dismiss()
                    val text = "Choose"
                    tv_pembimbing.text = text
                }

                val rv = view.findViewById<RecyclerView>(R.id.rv_dosen)
                rv.layoutManager = LinearLayoutManager(this)

                val ref = FirebaseDatabase.getInstance().getReference("Pembimbing")
                        .child(prodi).orderByChild("name")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ProposalActivity, "" + error.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        dataList.clear()
                        for (snapshot in p0.children) {
                            dataList.add(snapshot.getValue(Pembimbing::class.java)!!)
                        }
                        rv.adapter = OptionDosenAdapter(dataList) {
                            alert.dismiss()
                            tv_pembimbing.text = preferences.getValues("nameD").toString()
                        }
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        if (preferences.getValues("tap").equals("0")) {
            preferences.setValues("tap", "1")
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
        } else if (preferences.getValues("tap").equals("1")) {
            preferences.setValues("tap", "0")
            super.onBackPressed()
        }
    }
}