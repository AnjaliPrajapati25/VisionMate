package com.visionmate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnGetStarted =
            findViewById<Button>(R.id.btnGetStarted)

        btnGetStarted.setOnClickListener {

            val intent = Intent(
                this,
                HomeActivity::class.java
            )

            startActivity(intent)
        }
    }
}

