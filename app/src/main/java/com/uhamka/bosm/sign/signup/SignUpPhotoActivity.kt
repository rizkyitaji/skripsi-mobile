@file:Suppress("DEPRECATION")

package com.uhamka.bosm.sign.signup

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.model.Users
import com.uhamka.bosm.utils.Preferences
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_sign_up_photo.*

class SignUpPhotoActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var name: String
    lateinit var prodi: String
    lateinit var pass: String
    lateinit var photo: String
    lateinit var phone: String
    lateinit var email: String
    lateinit var whatsapp: String
    lateinit var filePath: Uri
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_photo)

        preferences = Preferences(this)
        preferences.setValues("upload", "0")

        id = preferences.getValues("idLogin").toString()
        name = preferences.getValues("nameLogin").toString()
        prodi = preferences.getValues("prodiLogin").toString()
        pass = preferences.getValues("pass").toString()
        phone = preferences.getValues("phone").toString()
        email = preferences.getValues("email").toString()
        whatsapp = preferences.getValues("whatsapp").toString()

        if (preferences.getValues("user").equals("mhs")) {
            ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Mahasiswa").child(id)
        } else {
            ref = FirebaseDatabase.getInstance().getReference("Users")
                    .child("Dosen").child(id)
        }

        val welcome = "Welcome !\n" + name
        tv_welcome.text = welcome

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_upload.setOnClickListener {
            if (preferences.getValues("upload").equals("0")) {
                showDialog()
            } else if (preferences.getValues("upload").equals("1")) {
                preferences.setValues("upload", "0")
                btn_upload.setImageResource(R.drawable.ic_btn_upload)
                iv_profile.setImageResource(R.drawable.ic_account_circle)
                btn_save.visibility = View.INVISIBLE
            }
        }

        btn_save.setOnClickListener {
            saveImage()
        }

        btn_skip.setOnClickListener {
            preferences.setValues("uri", "null")
            saveData()
        }
    }

    private fun showDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val v = layoutInflater.inflate(R.layout.layout_upload_photo, null)
        dialog.setContentView(v)
        dialog.show()

        val close = v.findViewById<ImageView>(R.id.iv_close)
        dialog.setCancelable(false)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val delete = v.findViewById<LinearLayout>(R.id.delete)
        delete.visibility = View.GONE

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //try without activity.result
        if (resultCode == RESULT_OK) {
            filePath = data?.data!!
            Glide.with(this)
                    .load(filePath)
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv_profile)

            preferences.setValues("upload", "1")
            btn_upload.setImageResource(R.drawable.ic_btn_delete)
            btn_save.visibility = View.VISIBLE
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImage() {
        val dialog = SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .build()

        dialog.show()

        val imgRef = FirebaseStorage.getInstance().getReference("images/" + id)
        imgRef.putFile(filePath)
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    dialog.setMessage("Uploaded " + progress.toInt() + "%")
                }

                .addOnFailureListener { e ->
                    dialog.dismiss()
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }

                .addOnSuccessListener {
                    imgRef.downloadUrl.addOnSuccessListener {
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                ref.child("photo").setValue(it.toString())
                                preferences.setValues("uri", it.toString())
                                saveData()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@SignUpPhotoActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    dialog.dismiss()
                    welcome()
                }
    }

    private fun saveData() {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ref.child("id").setValue(id)
                ref.child("name").setValue(name)
                ref.child("prodi").setValue(prodi)
                ref.child("password").setValue(pass)
                if (!phone.equals("")) {
                    ref.child("email").setValue(email)
                    ref.child("phone").setValue("0" + phone)
                    ref.child("whatsapp").setValue(whatsapp)
                }
                welcome()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignUpPhotoActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun welcome() {
        preferences.setValues("tap", "0")
        Toast.makeText(this, "Selamat Datang", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, HomeActivity::class.java)
                .putExtra("from", "login"))
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
        Toast.makeText(this, "Tekan tombol upload later untuk mengunggah foto Anda nanti", Toast.LENGTH_SHORT).show()
    }
}