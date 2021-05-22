@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.proposal.add

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.dashboard.proposal.DetailSkripsiActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_add_success.*

class AddSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_success)

        val preferences = Preferences(this)

        if (intent.getStringExtra("notif")!!.equals("add")) {
            tv_notif.text = getString(R.string.notif)
        }

        btn_home.setOnClickListener {
            preferences.setValues("tap", "0")
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra("from", "other"))
        }

        btn_detail.setOnClickListener {
            if (isConnected()) {
                startActivity(Intent(this, DetailSkripsiActivity::class.java)
                        .putExtra("from", "add"))
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
        Toast.makeText(this, "Tekan tombol View Proposal untuk melihat detail proposal atau tekan tombol Home untuk menuju beranda", Toast.LENGTH_LONG).show()
    }
}