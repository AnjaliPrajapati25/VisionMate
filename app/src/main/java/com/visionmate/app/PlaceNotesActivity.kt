package com.visionmate.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import com.visionmate.app.database.DatabaseHelper
import com.visionmate.app.utils.TextToSpeechHelper

class PlaceNotesActivity : Activity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var placesContainer: LinearLayout
    private lateinit var searchInput: EditText

    private lateinit var ttsHelper: TextToSpeechHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_notes)

        databaseHelper = DatabaseHelper(this)
        ttsHelper = TextToSpeechHelper(this)

        placesContainer = findViewById(R.id.placesContainer)
        searchInput = findViewById(R.id.etSearchPlace)

        val btnAddPlace = findViewById<Button>(R.id.btnAddPlace)

        btnAddPlace.setOnClickListener {
            showPlaceDialog()
        }

        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                loadPlaces(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadPlaces()
    }

    private fun showPlaceDialog(
        placeId: Int? = null,
        oldName: String = "",
        oldAddress: String = "",
        oldDescription: String = ""
    ) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 10, 40, 10)
        }

        val nameInput = EditText(this).apply {
            hint = "Place name"
            setText(oldName)
            contentDescription = "Enter place name"
        }

        val addressInput = EditText(this).apply {
            hint = "Address"
            setText(oldAddress)
            contentDescription = "Enter place address"
        }

        val descriptionInput = EditText(this).apply {
            hint = "Place description or directions"
            setText(oldDescription)
            minLines = 3
            gravity = Gravity.TOP
            contentDescription = "Enter place description"
        }

        layout.addView(nameInput)
        layout.addView(addressInput)
        layout.addView(descriptionInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (placeId == null)
                    "Add New Place"
                else
                    "Edit Place"
            )
            .setView(layout)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val name = nameInput.text.toString().trim()
                    val address = addressInput.text.toString().trim()
                    val description =
                        descriptionInput.text.toString().trim()

                    if (name.isEmpty()) {
                        nameInput.error = "Enter place name"
                        return@setOnClickListener
                    }

                    val success = if (placeId == null) {
                        databaseHelper.addPlaceNote(
                            name,
                            address,
                            description
                        )
                    } else {
                        databaseHelper.updatePlaceNote(
                            placeId,
                            name,
                            address,
                            description
                        )
                    }

                    if (success) {
                        Toast.makeText(
                            this,
                            "Place note saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                        loadPlaces(searchInput.text.toString())
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to save place note",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    private fun loadPlaces(searchQuery: String = "") {

        placesContainer.removeAllViews()

        val cursor = databaseHelper.getAllPlaceNotes()
        var found = false

        cursor.use {

            while (it.moveToNext()) {

                val id = it.getInt(
                    it.getColumnIndexOrThrow(
                        DatabaseHelper.COL_PLACE_ID
                    )
                )

                val name = it.getString(
                    it.getColumnIndexOrThrow(
                        DatabaseHelper.COL_PLACE_NAME
                    )
                ) ?: ""

                val address = it.getString(
                    it.getColumnIndexOrThrow(
                        DatabaseHelper.COL_ADDRESS
                    )
                ) ?: ""

                val description = it.getString(
                    it.getColumnIndexOrThrow(
                        DatabaseHelper.COL_DESCRIPTION
                    )
                ) ?: ""

                val matches = name.contains(
                    searchQuery.trim(),
                    ignoreCase = true
                ) || address.contains(
                    searchQuery.trim(),
                    ignoreCase = true
                ) || description.contains(
                    searchQuery.trim(),
                    ignoreCase = true
                )

                if (matches) {
                    found = true

                    addPlaceCard(
                        id,
                        name,
                        address,
                        description
                    )
                }
            }
        }

        if (!found) {
            val emptyMessage = TextView(this).apply {
                text = if (searchQuery.isBlank()) {
                    "No places saved yet."
                } else {
                    "No matching places found."
                }

                textSize = 18f
                setTextColor(0xFF475569.toInt())
            }

            placesContainer.addView(emptyMessage)
        }
    }

    private fun addPlaceCard(
        id: Int,
        name: String,
        address: String,
        description: String
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(0xFFFFFFFF.toInt())

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 18
            }
        }

        val details = TextView(this).apply {
            text = "$name\n$address\n$description"
            textSize = 18f
            setTextColor(0xFF102B50.toInt())
            setLineSpacing(5f, 1f)
            contentDescription =
                "Place $name. Address: $address. Details: $description"
        }

        card.addView(details)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val readButton = Button(this).apply {
            text = "Read Aloud"
            contentDescription = "Read $name aloud"

            setOnClickListener {

                val speechText = """
            Place name: $name.
            Address: $address.
            Description: $description.
        """.trimIndent()

                ttsHelper.speak(speechText)
            }
        }

        val stopButton = Button(this).apply {
            text = "Stop Speech"
            contentDescription = "Stop reading aloud"

            setOnClickListener {
                ttsHelper.stop()
            }
        }

        val editButton = Button(this).apply {
            text = "Edit"
            contentDescription = "Edit $name"
            setOnClickListener {
                showPlaceDialog(
                    id,
                    name,
                    address,
                    description
                )
            }
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            contentDescription = "Delete $name"
            setOnClickListener {
                confirmDelete(id, name)
            }
        }

        actions.addView(readButton)
        actions.addView(stopButton)
        actions.addView(editButton)
        actions.addView(deleteButton)

        card.addView(actions)
        placesContainer.addView(card)
    }

    private fun confirmDelete(id: Int, name: String) {

        AlertDialog.Builder(this)
            .setTitle("Delete Place")
            .setMessage("Are you sure you want to delete $name?")
            .setPositiveButton("Delete") { _, _ ->

                val success = databaseHelper.deletePlaceNote(id)

                if (success) {
                    Toast.makeText(
                        this,
                        "Place note deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadPlaces(searchInput.text.toString())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        databaseHelper.close()
        super.onDestroy()
    }
}
