@file:Suppress("DEPRECATION")

package com.uhamka.bosm.setting.help

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import kotlinx.android.synthetic.main.activity_detail_faq.*
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DetailFAQActivity : AppCompatActivity() {

    lateinit var item: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_faq)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        item = intent.getStringExtra("item")!!
        if (item.equals("1")) {
            tv_question.text = getString(R.string.bagaimana_cara_mengajukan_proposal)
        } else if (item.equals("2")) {
            tv_question.text = getString(R.string.bagaimana_cara_mengedit_proposal_atau_mengganti_dosen_pembimbing)
        } else if (item.equals("3")) {
            tv_question.text = getString(R.string.bagaimana_cara_menghindari_kesamaan_topik_penelitian_saat_mengajukan_proposal)
        } else if (item.equals("4")) {
            tv_question.text = getString(R.string.bagaimana_saya_dapat_memulai_bimbingan)
        } else if (item.equals("5")) {
            tv_question.text = getString(R.string.bagaimana_cara_menghubungi_dosen_pembimbing)
        } else if (item.equals("6")) {
            tv_question.text = getString(R.string.bagaimana_cara_menghubungi_mahasiswa)
        } else if (item.equals("7")) {
            tv_question.text = getString(R.string.dimana_saya_dapat_melihat_catatan_bimbingan)
        }

        val ref = FirebaseDatabase.getInstance().getReference("FAQ")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uri = snapshot.child("pdf").value.toString()
                RetrievePdf().execute(uri)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailFAQActivity, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class RetrievePdf : AsyncTask<String, Void, InputStream>() {
        override fun doInBackground(vararg strings: String?): InputStream? {
            var inputStream: InputStream? = null
            try {
                val url = URL(strings[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                if (urlConnection.responseCode == 200) {
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            } catch (e: IOException) {
                return null
            }
            return inputStream
        }

        override fun onPostExecute(result: InputStream?) {
            if (item.equals("1")) {
                pdfView.fromStream(result)
                        .pages(0, 1, 2, 3)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("2")){
                pdfView.fromStream(result)
                        .pages(4)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("3")){
                pdfView.fromStream(result)
                        .pages(5)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("4")){
                pdfView.fromStream(result)
                        .pages(6, 7)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("5")){
                pdfView.fromStream(result)
                        .pages(8)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("6")){
                pdfView.fromStream(result)
                        .pages(9, 10)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            } else if (item.equals("7")){
                pdfView.fromStream(result)
                        .pages(11)
                        .onRender(object : OnRenderListener {
                            override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                                pdfView.fitToWidth()
                            }
                        })
                        .load()
            }
        }
    }
}