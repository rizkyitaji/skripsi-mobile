@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.bimbingan

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.utils.Preferences
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_catatan_bimbingan.*

class CatatanBimbinganActivity : AppCompatActivity() {

    lateinit var uri: String
    lateinit var nim: String
    lateinit var date: String
    lateinit var ref: DatabaseReference
    lateinit var path: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catatan_bimbingan)

        preferences = Preferences(this)
        nim = preferences.getValues("nim").toString()
        ref = FirebaseDatabase.getInstance().getReference("Bimbingan")

        btn_back.setOnClickListener {
            onBackPressed()
        }

        date = preferences.getValues("date").toString()
        tv_title.text = preferences.getValues("title").toString()
        tv_date.text = date

        val bimbingan = intent.getStringExtra("bimbingan")!!
        if (bimbingan.equals("offline")) {
            cv_revisi.visibility = View.GONE
            tv_revisi.visibility = View.VISIBLE
        } else if (bimbingan.equals("online")) {
            tv_revisi.visibility = View.GONE
            cv_revisi.visibility = View.VISIBLE

            if (preferences.getValues("user").equals("mhs")) {
                ul_revisi.visibility = View.GONE
                dl_revisi.visibility = View.VISIBLE
                textRevisi.text = getString(R.string.download_hasil_revisi_di_sini)
            } else if (preferences.getValues("user").equals("dosen")) {
                dl_revisi.visibility = View.GONE
                ul_revisi.visibility = View.VISIBLE
                textRevisi.text = getString(R.string.upload_hasil_revisi_di_sini)
            }
        }

        tv_revisi.text = preferences.getValues("revisi").toString()

        dl_revisi.setOnClickListener {
            if (preferences.getValues("docs").equals("")) {
                Toast.makeText(this, "Revisi Laporan belum diupload oleh dosen pembimbing Anda", Toast.LENGTH_LONG).show()
            } else {
                downloadRevisi()
                Toast.makeText(this, "Downloading Revisi_" + date + "_" + nim + ".docx", Toast.LENGTH_LONG).show()
            }
        }

        ul_revisi.setOnClickListener {
            val intent = Intent().apply {
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Document"), 1)
        }

        btn_download.setOnClickListener {
            downloadNote()
            Toast.makeText(this, "Downloading Lembar Bimbingan_" + nim + ".docx", Toast.LENGTH_LONG).show()
        }

        btn_upload.setOnClickListener {
            val intent = Intent().apply {
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Document"), 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val filePath = data?.data!!
            if (requestCode == 1) {
                val document = "Revisi_" + date + "_" + nim
                uploadData(filePath, document, "revisi")
            } else if (requestCode == 2) {
                val document = "Lembar Bimbingan_" + nim
                uploadData(filePath, document, "note")
            }
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadData(filePath: Uri, document: String, mText: String) {
        val dialog = SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build()
        dialog.show()

        val docsRef = FirebaseStorage.getInstance().getReference("docs/" + document)
        docsRef.putFile(filePath)
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    dialog.setMessage("Uploaded " + progress.toInt() + "%")
                }

                .addOnFailureListener { e ->
                    dialog.dismiss()
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }

                .addOnSuccessListener {
                    docsRef.downloadUrl.addOnSuccessListener {
                        if (mText.equals("revisi")) {
                            path = ref.child(nim).child(date)
                        } else {
                            path = ref.child(nim).child("Lembar Bimbingan")
                        }

                        path.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                path.child("docs").setValue(it.toString())
                                Toast.makeText(this@CatatanBimbinganActivity, "Dokumen berhasil diunggah", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@CatatanBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
    }

    private fun downloadRevisi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 10)
        } else {
            val title = "Revisi_Skripsi_" + date + "_" + nim
            val uri = preferences.getValues("docs").toString()
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

    private fun downloadNote() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 20)
        } else {
            val note = ref.child(nim).child("Lembar Bimbingan")
            note.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val getUri = snapshot.child("docs").value.toString()
                    if (!getUri.equals("null")) {
                        uri = getUri
                    } else {
                        val db = ref.child("Lembar Bimbingan")
                        db.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                uri = dataSnapshot.child("docs").value.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@CatatanBimbinganActivity, ""+error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CatatanBimbinganActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })

            val title = "Lembar Bimbingan_" + nim
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
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadRevisi()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 20) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadNote()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}