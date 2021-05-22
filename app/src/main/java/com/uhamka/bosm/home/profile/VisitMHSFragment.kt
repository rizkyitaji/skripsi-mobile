@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.profile

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.setting.SettingActivity
import com.uhamka.bosm.utils.Preferences

class VisitMHSFragment : Fragment() {

    lateinit var uri: String
    lateinit var name: String
    lateinit var nim: String
    lateinit var prodi: String
    lateinit var getPhone: String
    lateinit var preferences: Preferences
    val REQUEST_CALL_PHONE = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_visit_mhs, container, false)

        preferences = Preferences(context!!)

        /*set profile*/
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val setting = view.findViewById<ImageView>(R.id.iv_setting)
        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        val tv_nim = view.findViewById<TextView>(R.id.tv_nim)
        val tv_prodi = view.findViewById<TextView>(R.id.tv_prodi)

        if (preferences.getValues("action").equals("profile")) {
            uri = preferences.getValues("uri").toString()
            name = preferences.getValues("nameLogin").toString()
            nim = preferences.getValues("idLogin").toString()
            prodi = preferences.getValues("prodiLogin").toString()
        } else if (preferences.getValues("action").equals("view")) {
            uri = preferences.getValues("photo").toString()
            name = preferences.getValues("name").toString()
            nim = preferences.getValues("id").toString()
            prodi = preferences.getValues("prodi").toString()
            setting.visibility = View.INVISIBLE
        }

        /*goto setting*/
        setting.setOnClickListener {
            startActivity(Intent(activity, SettingActivity::class.java))
        }

        profile.setOnClickListener {
            showPhoto(uri)
        }

        /*set contact*/
        val phone = view.findViewById<ImageView>(R.id.iv_phone)
        val email = view.findViewById<ImageView>(R.id.iv_email)
        val whatsapp = view.findViewById<ImageView>(R.id.iv_whatsapp)

        val userDB = FirebaseDatabase.getInstance().getReference("Users")
                .child("Mahasiswa").child(nim)
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                getPhone = snapshot.child("phone").getValue().toString()
                val getWA = snapshot.child("whatsapp").getValue().toString()
                val getEmail = snapshot.child("email").getValue().toString()

                if (preferences.getValues("action").equals("profile")) {
                    phone.setOnClickListener {
                        if (isConnected()) {
                            if (!getPhone.equals("null")) {
                                Toast.makeText(context, "Phone : " + getPhone, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Silakan tambahkan nomor telepon Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }

                    whatsapp.setOnClickListener {
                        if (isConnected()) {
                            if (!getWA.equals("null")) {
                                Toast.makeText(context, "Whatsapp : 0" + getWA, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Silakan tambahkan nomor whatsapp Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }

                    email.setOnClickListener {
                        if (isConnected()) {
                            if (!getEmail.equals("null")) {
                                Toast.makeText(context, "Email : " + getEmail, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Silakan tambahkan alamat email Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (preferences.getValues("action").equals("view")) {
                    phone.setOnClickListener {
                        phoneCall()
                    }

                    whatsapp.setOnClickListener {
                        if (!getWA.equals("null")) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/=62" + getWA)))
                        } else {
                            Toast.makeText(context, "Nomor whatsapp tidak tersedia", Toast.LENGTH_LONG).show()
                        }
                    }

                    email.setOnClickListener {
                        if (!getEmail.equals("null")) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getEmail)))
                        } else {
                            Toast.makeText(context, "Alamat email tidak tersedia", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })

        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(context!!)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile)
            }

            tv_name.text = name
            tv_nim.text = nim
            tv_prodi.text = prodi

            /*set content*/
            val tv_dosen = view.findViewById<TextView>(R.id.tv_dosen)
            val tv_title = view.findViewById<TextView>(R.id.tv_title)
            val tv_desc = view.findViewById<TextView>(R.id.tv_desc)

            val ref = FirebaseDatabase.getInstance()
                    .getReference("Repository").child(prodi).child(nim)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val dosen = snapshot.child("pembimbing").value.toString()
                    val title = snapshot.child("judul").value.toString()
                    val desc = snapshot.child("desc").value.toString()

                    if (!title.equals("null")) {
                        tv_dosen.text = dosen
                        tv_title.text = title
                        tv_desc.text = desc
                    }
                }
            })
        } else {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

        val content = view.findViewById<CardView>(R.id.cv_content)
        val expandableView = view.findViewById<ConstraintLayout>(R.id.expandableView)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)
        expand.setOnClickListener {
            if (expandableView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(content, AutoTransition())
                expandableView.visibility = View.VISIBLE
                expand.setImageResource(R.drawable.ic_expand_less)
            } else {
                TransitionManager.beginDelayedTransition(content, AutoTransition())
                expandableView.visibility = View.GONE
                expand.setImageResource(R.drawable.ic_expand_more)
            }
        }

        return view
    }

    private fun showPhoto(uri: String) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.layout_show_photo, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val profile = view.findViewById<ImageView>(R.id.iv_photo)
        if (!uri.equals("null")) {
            Glide.with(context!!)
                    .load(uri)
                    .into(profile)
        }
    }

    private fun phoneCall() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
        } else {
            if (!getPhone.equals("null")) {
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getPhone)))
            } else {
                Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phoneCall()
            } else {
                Toast.makeText(context, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isConnected(): Boolean {
        val connectivity = activity!!.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }
}
