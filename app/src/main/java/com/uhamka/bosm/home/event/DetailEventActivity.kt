@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.event

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_detail_event.*
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DetailEventActivity : AppCompatActivity() {

    lateinit var title: String
    lateinit var preferences: Preferences

    companion object {
        const val REQUEST_STRORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_event)

        preferences = Preferences(this)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        title = preferences.getValues("title").toString()
        tv_title.text = title

        val event = intent.getStringExtra("event")!!
        if (event.equals("detail")) {
            sv_detail.visibility = View.VISIBLE
            pdfView.visibility = View.GONE
            btn_download.visibility = View.GONE
            tv_detail.text = preferences.getValues("detail").toString()
        } else if (event.equals("download")) {
            sv_detail.visibility = View.GONE
            pdfView.visibility = View.VISIBLE
            btn_download.visibility = View.VISIBLE
            val uri = preferences.getValues("pdf").toString()
            RetrievePdf().execute(uri)
        }

        btn_download.setOnClickListener {
            downloadPdf()
            Toast.makeText(this, "Downloading " + title + ".pdf", Toast.LENGTH_LONG).show()
        }
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
            pdfView.fromStream(result)
                    .pages(0)
                    .onRender(object : OnRenderListener {
                        override fun onInitiallyRendered(nbPages: Int, pageWidth: Float, pageHeight: Float) {
                            pdfView.fitToWidth(0)
                        }
                    })
                    .load()
        }
    }

    private fun downloadPdf() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STRORAGE)
        } else {
            val uri = preferences.getValues("pdf").toString()
            val request = DownloadManager.Request(Uri.parse(uri)).apply {
                setTitle(title)
                setDescription(uri)
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val manager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_STRORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadPdf()
            } else {
                Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
