@file:Suppress("DEPRECATION")

package com.uhamka.bosm.setting

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.setting.account.EditAccountActivity
import com.uhamka.bosm.setting.help.FAQActivity
import com.uhamka.bosm.sign.signin.SignInActivity
import com.uhamka.bosm.utils.Preferences
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        preferences = Preferences(this)

        id = preferences.getValues("idLogin").toString()
        val uri = preferences.getValues("uri").toString()
        val name = preferences.getValues("nameLogin").toString()
        val prodi = preferences.getValues("prodiLogin").toString()

        if (preferences.getValues("user").equals("mhs")) {
            tv_prodi.visibility = View.VISIBLE
            ref = FirebaseDatabase.getInstance().getReference("Users").child("Mahasiswa").child(id)
            val param = tv_id.layoutParams as LinearLayout.LayoutParams
            param.setMargins(0, convert(8), 0, 0)
        } else if (preferences.getValues("user").equals("dosen")) {
            tv_prodi.visibility = View.GONE
            ref = FirebaseDatabase.getInstance().getReference("Users").child("Dosen").child(id)
            val param = tv_id.layoutParams as LinearLayout.LayoutParams
            param.setMargins(0, convert(16), 0, 0)
        }

        tv_name.text = name
        tv_id.text = id
        tv_prodi.text = prodi

        btn_back.setOnClickListener {
            onBackPressed()
        }

        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(iv_profile)
            }
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

        iv_profile.setOnClickListener {
            showPhoto()
        }

        ib_cam.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val v = layoutInflater.inflate(R.layout.layout_upload_photo, null)
            dialog.setContentView(v)
            dialog.show()

            val close = v.findViewById<ImageView>(R.id.iv_close)
            dialog.setCancelable(false)
            close.setOnClickListener {
                dialog.dismiss()
            }

            val camera = v.findViewById<LinearLayout>(R.id.camera)
            camera.setOnClickListener {
                if (isConnected()) {
                    ImagePicker.with(this)
                            .cameraOnly()
                            .cropSquare()
                            .start()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }

            val gallery = v.findViewById<LinearLayout>(R.id.gallery)
            gallery.setOnClickListener {
                if (isConnected()) {
                    ImagePicker.with(this)
                            .galleryOnly()
                            .cropSquare()
                            .start()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }

            val delete = v.findViewById<LinearLayout>(R.id.delete)
            delete.setOnClickListener {
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val photo = snapshot.child("photo").value.toString()
                        if (!photo.equals("null")) {
                            preferences.setValues("uri", "0")
                            ref.child("photo").removeValue()

                            val imgRef = FirebaseStorage.getInstance().getReference("images/" + id)
                            imgRef.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this@SettingActivity, "Foto telah dihapus", Toast.LENGTH_SHORT).show()
                                    }

                                    .addOnFailureListener {
                                        Toast.makeText(this@SettingActivity, "Error", Toast.LENGTH_SHORT).show()
                                    }
                        } else {
                            Toast.makeText(this@SettingActivity, "Anda belum mengunggah foto", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SettingActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        tv_password.setOnClickListener {
            startActivity(Intent(this, EditAccountActivity::class.java)
                    .putExtra("edit", "password"))
        }

        tv_phone.setOnClickListener {
            startActivity(Intent(this, EditAccountActivity::class.java)
                    .putExtra("edit", "phone"))
        }

        tv_whatsapp.setOnClickListener {
            startActivity(Intent(this, EditAccountActivity::class.java)
                    .putExtra("edit", "whatsapp"))
        }

        tv_email.setOnClickListener {
            startActivity(Intent(this, EditAccountActivity::class.java)
                    .putExtra("edit", "email"))
        }

        tv_faq.setOnClickListener {
            startActivity(Intent(this, FAQActivity::class.java))
        }

        tv_contact.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.layout_contact_us, null)
            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            dialog.show()

            val close = view.findViewById<ImageView>(R.id.iv_close)
            close.setOnClickListener {
                dialog.dismiss()
            }

            val email = view.findViewById<LinearLayout>(R.id.email)
            email.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:ft@uhamka.ac.id")))
            }

            val phone = view.findViewById<LinearLayout>(R.id.phone)
            phone.setOnClickListener {
                phoneCall()
            }

            val whatsapp = view.findViewById<LinearLayout>(R.id.whatsapp)
            whatsapp.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/=6285813001500")))
            }

            val dev = view.findViewById<LinearLayout>(R.id.dev)
            dev.setOnClickListener {
                val nim = "1603015016"
                val user = FirebaseDatabase.getInstance()
                        .getReference("Users").child("Mahasiswa").child(nim)
                user.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val photo = snapshot.child("photo").value.toString()

                        preferences.setValues("id", nim)
                        preferences.setValues("photo", photo)
                        preferences.setValues("name", "Rizky Putra Itaji")
                        preferences.setValues("prodi", "Teknik Informatika")
                        startActivity(Intent(this@SettingActivity, HomeActivity::class.java)
                                .putExtra("from", "listMHS"))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SettingActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        btn_logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        if (isConnected()) {
                            preferences.setValues("status", "0")
                            preferences.setValues("proposal", "0")
                            startActivity(Intent(this, SignInActivity::class.java))
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
    }

    private fun phoneCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        } else {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:0218400941")))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phoneCall()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhoto() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_show_photo, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val profile = view.findViewById<ImageView>(R.id.iv_photo)
        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uri = snapshot.child("photo").value.toString()
                    if (!uri.equals("null")) {
                        Glide.with(this@SettingActivity)
                                .load(uri)
                                .into(profile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SettingActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val dialog = SpotsDialog.Builder()
                    .setContext(this)
                    .setCancelable(false)
                    .build()

            dialog.show()
            val filePath = data?.data!!

            val imgRef = FirebaseStorage.getInstance().getReference("images/" + id)
            imgRef.putFile(filePath)
                    .addOnProgressListener { taskSnapshot ->
                        preferences.setValues("proses", "1")
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploaded " + progress.toInt() + "%")
                    }

                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                    }

                    .addOnSuccessListener {
                        imgRef.downloadUrl.addOnSuccessListener {
                            ref.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    preferences.setValues("uri", it.toString())
                                    ref.child("photo").setValue(it.toString())
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@SettingActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                                }
                            })
                        }

                        dialog.dismiss()
                        preferences.setValues("proses", "0")
                        Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_LONG).show()
                        Glide.with(this)
                                .load(filePath)
                                .apply(RequestOptions.circleCropTransform())
                                .into(iv_profile)
                    }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convert(dp: Int): Int {
        return (dp * this.resources.displayMetrics.density + 0.5).toInt()
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
        if (preferences.getValues("proses").equals("1")) {
            Toast.makeText(this, "Silakan tunggu sampai foto berhasil diunggah", Toast.LENGTH_LONG).show()
        } else {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uri = snapshot.child("photo").value.toString()
                    preferences.setValues("uri", uri)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SettingActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra("from", "setting_" + preferences.getValues("user")))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}