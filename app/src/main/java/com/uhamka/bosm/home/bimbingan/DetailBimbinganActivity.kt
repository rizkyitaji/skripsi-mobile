@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.bimbingan

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.utils.DividerItemDecorator
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.model.Bimbingan
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_detail_bimbingan.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailBimbinganActivity : AppCompatActivity() {

    lateinit var name: String
    lateinit var photo: String
    lateinit var setMonth: String
    lateinit var preferences: Preferences
    private var dataList = ArrayList<Bimbingan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_bimbingan)

        preferences = Preferences(this)

        name = preferences.getValues("name").toString()
        val id = preferences.getValues("id").toString()
        val nim = preferences.getValues("nim").toString()
        val prodi = preferences.getValues("prodi").toString()
        val title = preferences.getValues("title").toString()

        btn_back.setOnClickListener {
            onBackPressed()
        }

        tv_name.text = name
        tv_nim.text = nim
        tv_prodi.text = prodi
        tv_title.text = title

        val user = FirebaseDatabase.getInstance()
                .getReference("Users").child("Mahasiswa").child(nim)
        user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                photo = snapshot.child("photo").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        btn_view.setOnClickListener {
            preferences.setValues("id", nim)
            preferences.setValues("tap", "2")
            preferences.setValues("name", name)
            preferences.setValues("prodi", prodi)
            preferences.setValues("photo", photo)
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra("from", "listMHS"))
        }

        btn_lulus.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah Anda ingin menandai bahwa " + name + " sudah lulus?")
                    .setPositiveButton("Ya") { _, _ ->
                        if (isConnected()) {
                            val proposal = FirebaseDatabase.getInstance().getReference("Proposal")
                            proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    proposal.child(id).child("status").setValue("LULUS")
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@DetailBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                                }
                            })

                            val status = FirebaseDatabase.getInstance()
                                    .getReference("Status").child(nim).child(id)
                            status.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    status.child("status").setValue("LULUS")
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@DetailBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                                }
                            })

                            Toast.makeText(this, "Mahasiswa bimbingan Anda yang bernama " +
                                    name + " telah dinyatakan LULUS!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }

        rv_bimbingan.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider)))

        val ref = FirebaseDatabase.getInstance().getReference("Bimbingan").child(nim)

        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(Bimbingan::class.java)
                        if (item != null) {
                            tv_none.visibility = View.GONE
                            dataList.add(item)
                        } else {
                            tv_none.visibility = View.VISIBLE
                        }
                    }
                    rv_bimbingan.adapter = BimbinganAdapter(dataList) {
                        if (preferences.getValues("action").equals("note")) {
                            if (preferences.getValues("laporan").equals("")) {
                                startActivity(Intent(this@DetailBimbinganActivity, CatatanBimbinganActivity::class.java)
                                        .putExtra("bimbingan", "offline"))
                            } else {
                                startActivity(Intent(this@DetailBimbinganActivity, CatatanBimbinganActivity::class.java)
                                        .putExtra("bimbingan", "online"))
                            }
                        } else {
                            downloadData()
                        }
                    }
                }
            })
        } else {
            tv_none.text = getString(R.string.tidak_ada_koneksi_internet)
        }

        val lastChild = ref.orderByKey().limitToLast(1)
        lastChild.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val day = dataSnapshot.child("day").value.toString()
                    val month = dataSnapshot.child("month").value.toString()
                    if (!day.equals("null")) {
                        compareDate(day, month)
                    }
                }
            }
        })
    }

    private fun downloadData() {
        val date = preferences.getValues("date").toString()
        val uri = preferences.getValues("laporan").toString()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            val title = "Laporan_Skripsi_" + date + "_" + name
            val request = DownloadManager.Request(Uri.parse(uri)).apply {
                setTitle(title)
                setDescription(uri)
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".docx")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val manager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadData()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
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

    private fun compareDate(day: String, month: String) {
        val currentDay = SimpleDateFormat("dd", Locale.ENGLISH).format(Date())
        val currentMonth = SimpleDateFormat("MMM", Locale.ENGLISH).format(Date())

        if (currentMonth.equals("Jan")) {
            setMonth = "01"
        } else if (currentMonth.equals("Feb")) {
            setMonth = "02"
        } else if (currentMonth.equals("Mar")) {
            setMonth = "03"
        } else if (currentMonth.equals("Apr")) {
            setMonth = "04"
        } else if (currentMonth.equals("May")) {
            setMonth = "05"
        } else if (currentMonth.equals("Jun")) {
            setMonth = "06"
        } else if (currentMonth.equals("Jul")) {
            setMonth = "07"
        } else if (currentMonth.equals("Aug")) {
            setMonth = "08"
        } else if (currentMonth.equals("Sep")) {
            setMonth = "09"
        } else if (currentMonth.equals("Oct")) {
            setMonth = "10"
        } else if (currentMonth.equals("Nov")) {
            setMonth = "11"
        } else if (currentMonth.equals("Dec")) {
            setMonth = "12"
        }

        val currentMonthInt = setMonth.toInt()
        val currentDayInt = currentDay.toInt()
        val lastMonthInt = month.toInt()
        val lastDayInt = day.toInt()

        if (lastDayInt.equals(currentDayInt) && lastMonthInt < currentMonthInt) {
            val text = name + " belum melakukan bimbingan selama sebulan!"
            sendNotification(text)
        } else if (lastMonthInt < currentMonthInt) {
            val text = name + " belum melakukan bimbingan di bulan ini!"
            sendNotification(text)
        }
    }

    private fun sendNotification(text: String) {
        val id = "notif_bimbingan"
        val name = "KotlinApp"
        val description = "Notif_BOSM_APP"
        val NOTIFICATION_ID = 2
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, DetailBimbinganActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_school)
                .setContentTitle("BIMBINGAN SKRIPSI")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}