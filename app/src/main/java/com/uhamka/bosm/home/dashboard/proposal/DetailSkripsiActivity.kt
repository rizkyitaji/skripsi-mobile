@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.proposal

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.dashboard.proposal.add.AddSuccessActivity
import com.uhamka.bosm.home.repository.RepositoryActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_detail_skripsi.*

class DetailSkripsiActivity : AppCompatActivity() {

    lateinit var nim: String
    lateinit var name: String
    lateinit var photo: String
    lateinit var from: String
    lateinit var mDatabase: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_skripsi)

        preferences = Preferences(this)

        nim = preferences.getValues("nim").toString()
        val id = preferences.getValues("id").toString()
        val prodi = preferences.getValues("prodi").toString()
        from = intent.getStringExtra("from")!!

        if (from.equals("add")) {
            tv_title.text = getString(R.string.proposal)
            btn_view.visibility = View.GONE
            mDatabase = FirebaseDatabase.getInstance().getReference("Proposal").child(id)
        } else if (from.equals("home")) {
            tv_title.text = getString(R.string.proposal)
            btn_view.visibility = View.VISIBLE
            mDatabase = FirebaseDatabase.getInstance().getReference("Proposal").child(id)
        } else if (from.equals("repository")){
            tv_title.text = getString(R.string.skripsi)
            btn_view.visibility = View.VISIBLE
            mDatabase = FirebaseDatabase.getInstance().getReference("Repository").child(prodi).child(nim)
        } else {
            tv_title.text = getString(R.string.proposal)
            btn_view.visibility = View.VISIBLE
            mDatabase = FirebaseDatabase.getInstance().getReference("Skripsi").child(prodi).child(nim)
        }

        mDatabase.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailSkripsiActivity, "" + error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(data: DataSnapshot) {
                name = data.child("name").getValue().toString()
                val dosen = data.child("pembimbing").getValue().toString()
                val title = data.child("judul").getValue().toString()
                val desc = data.child("desc").getValue().toString()
                et_name.text = name
                et_nim.text = nim
                tv_prodi.text = prodi
                tv_dosen.text = dosen
                et_judul.text = title
                et_desc.text = desc

                val user = FirebaseDatabase.getInstance()
                        .getReference("Users").child("Mahasiswa").child(nim)
                user.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        photo = snapshot.child("photo").value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailSkripsiActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_home.setOnClickListener {
            preferences.setValues("tap", "0")
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra("from", "other"))
        }

        btn_view.setOnClickListener {
            if (isConnected()) {
                preferences.setValues("id", nim)
                preferences.setValues("name", name)
                preferences.setValues("prodi", prodi)
                preferences.setValues("photo", photo)
                startActivity(Intent(this, HomeActivity::class.java)
                        .putExtra("from", "listMHS"))
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