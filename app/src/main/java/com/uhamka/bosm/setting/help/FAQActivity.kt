@file:Suppress("DEPRECATION")

package com.uhamka.bosm.setting.help

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uhamka.bosm.R
import kotlinx.android.synthetic.main.activity_faq.*

class FAQActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        itemOne.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "1"))
        }

        itemTwo.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "2"))
        }

        itemThree.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "3"))
        }

        itemFour.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "4"))
        }

        itemFive.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "5"))
        }

        itemSix.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "6"))
        }

        itemSeven.setOnClickListener {
            startActivity(Intent(this, DetailFAQActivity::class.java)
                    .putExtra("item", "7"))
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
