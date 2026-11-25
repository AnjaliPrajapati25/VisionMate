package com.visionmate.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.visionmate.app.database.DatabaseHelper

class ContactsActivity : Activity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var contactsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        databaseHelper = DatabaseHelper(this)

        contactsContainer =
            findViewById(R.id.contactsContainer)

        val btnAddContact =
            findViewById<Button>(R.id.btnAddContact)

        btnAddContact.setOnClickListener {
            showContactDialog()
        }

        loadContacts()
    }

    private fun showContactDialog(
        contactId: Int? = null,
        oldName: String = "",
        oldPhone: String = "",
        oldRelationship: String = ""
    ) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 10, 40, 10)
        }

        val nameInput = EditText(this).apply {
            hint = "Contact name"
            setText(oldName)
            contentDescription = "Enter contact name"
        }

        val phoneInput = EditText(this).apply {
            hint = "Phone number"
            setText(oldPhone)
            inputType = 3
            contentDescription = "Enter phone number"
        }

        val relationshipInput = EditText(this).apply {
            hint = "Relationship (e.g. Father)"
            setText(oldRelationship)
            contentDescription = "Enter relationship"
        }

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(relationshipInput)

        AlertDialog.Builder(this)
            .setTitle(
                if (contactId == null)
                    "Add Emergency Contact"
                else
                    "Edit Emergency Contact"
            )
            .setView(layout)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->

                dialog.setOnShowListener {

                    dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener {

                        val name =
                            nameInput.text.toString().trim()

                        val phone =
                            phoneInput.text.toString().trim()

                        val relationship =
                            relationshipInput.text.toString().trim()

                        if (name.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(
                                this,
                                "Please enter name and phone number",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        val success = if (contactId == null) {
                            databaseHelper.addContact(
                                name,
                                phone,
                                relationship
                            )
                        } else {
                            databaseHelper.updateContact(
                                contactId,
                                name,
                                phone,
                                relationship
                            )
                        }

                        if (success) {
                            Toast.makeText(
                                this,
                                "Contact saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            dialog.dismiss()
                            loadContacts()
                        } else {
                            Toast.makeText(
                                this,
                                "Unable to save contact",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                dialog.show()
            }
    }

    private fun loadContacts() {

        contactsContainer.removeAllViews()

        val cursor = databaseHelper.getAllContacts()

        cursor.use {

            if (!it.moveToFirst()) {
                val emptyMessage = TextView(this).apply {
                    text = "No contacts saved yet."
                    textSize = 18f
                    setTextColor(0xFF475569.toInt())
                    contentDescription = text.toString()
                }

                contactsContainer.addView(emptyMessage)
                return
            }

            do {
                val id = it.getInt(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_ID)
                )

                val name = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)
                )

                val phone = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE)
                )

                val relationship = it.getString(
                    it.getColumnIndexOrThrow(
                        DatabaseHelper.COL_RELATIONSHIP
                    )
                ) ?: ""

                addContactCard(
                    id,
                    name,
                    phone,
                    relationship
                )

            } while (it.moveToNext())
        }
    }

    private fun addContactCard(
        id: Int,
        name: String,
        phone: String,
        relationship: String
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
            text = "$name\n$phone\n$relationship"
            textSize = 18f
            setTextColor(0xFF102B50.toInt())
            setLineSpacing(5f, 1f)
            contentDescription =
                "Contact $name, phone $phone, $relationship"
        }

        card.addView(details)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val callButton = Button(this).apply {
            text = "Call"
            contentDescription = "Call $name"
            setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )
                startActivity(intent)
            }
        }

        val editButton = Button(this).apply {
            text = "Edit"
            contentDescription = "Edit $name"
            setOnClickListener {
                showContactDialog(
                    id,
                    name,
                    phone,
                    relationship
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

        actions.addView(callButton)
        actions.addView(editButton)
        actions.addView(deleteButton)

        card.addView(actions)
        contactsContainer.addView(card)
    }

    private fun confirmDelete(id: Int, name: String) {

        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete $name?")
            .setPositiveButton("Delete") { _, _ ->

                val success = databaseHelper.deleteContact(id)

                if (success) {
                    Toast.makeText(
                        this,
                        "Contact deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadContacts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}
