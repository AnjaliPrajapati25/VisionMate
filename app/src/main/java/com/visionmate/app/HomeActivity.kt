package com.visionmate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class HomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val btnContacts =
            findViewById<Button>(R.id.btnContacts)

        val btnPlaceNotes =
            findViewById<Button>(R.id.btnPlaceNotes)

        val btnTasks =
            findViewById<Button>(R.id.btnTasks)

        val btnHelp =
            findViewById<Button>(R.id.btnHelp)

        btnContacts.setOnClickListener {

                val intent = Intent(
                    this,
                    ContactsActivity::class.java
                )

                startActivity(intent)
        }

        btnPlaceNotes.setOnClickListener {

                val intent = Intent(
                    this,
                    PlaceNotesActivity::class.java
                )

                startActivity(intent)
        }

        btnTasks.setOnClickListener {
                val intent = Intent(this, DailyTasksActivity::class.java)
                startActivity(intent)
        }

        btnHelp.setOnClickListener {
            showMessage("Help and Instructions")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            "$message module coming soon!",
            Toast.LENGTH_SHORT
        ).show()
    }
}
