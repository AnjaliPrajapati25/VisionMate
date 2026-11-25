package com.visionmate.app.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "visionmate.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_CONTACTS = "emergency_contacts"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PHONE = "phone"
        const val COL_RELATIONSHIP = "relationship"

        private const val TABLE_PLACE_NOTES = "place_notes"
        const val COL_PLACE_ID = "id"
        const val COL_PLACE_NAME = "place_name"
        const val COL_ADDRESS = "address"
        const val COL_DESCRIPTION = "description"
        const val COL_CREATED_AT = "created_at"

        const val TABLE_DAILY_TASKS = "daily_tasks"
        const val COL_TASK_ID = "id"
        const val COL_TASK_TITLE = "task_title"
        const val COL_TASK_DESCRIPTION = "task_description"
        const val COL_TASK_DATE = "task_date"
        const val COL_TASK_COMPLETED = "is_completed"

    }

    override fun onCreate(db: SQLiteDatabase) {

            // Create Emergency Contacts table
            val createContactsTable = """
        CREATE TABLE $TABLE_CONTACTS (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NAME TEXT NOT NULL,
            $COL_PHONE TEXT NOT NULL,
            $COL_RELATIONSHIP TEXT
        )
    """.trimIndent()

            db.execSQL(createContactsTable)

            // Create Personal Place Notes table
            createPlaceNotesTable(db)
            createDailyTasksTable(db)
    }
    private fun createPlaceNotesTable(db: SQLiteDatabase) {

        val createTable = """
        CREATE TABLE $TABLE_PLACE_NOTES (
            $COL_PLACE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_PLACE_NAME TEXT NOT NULL,
            $COL_ADDRESS TEXT,
            $COL_DESCRIPTION TEXT,
            $COL_CREATED_AT TEXT
        )
    """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 2) {
            createPlaceNotesTable(db)
        }
        if(oldVersion<3){
            createDailyTasksTable(db)
        }
    }

    fun addContact(
        name: String,
        phone: String,
        relationship: String
    ): Boolean {

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_PHONE, phone)
            put(COL_RELATIONSHIP, relationship)
        }

        val result = db.insert(TABLE_CONTACTS, null, values)
        return result != -1L
    }

    fun getAllContacts(): android.database.Cursor {

        return readableDatabase.query(
            TABLE_CONTACTS,
            null,
            null,
            null,
            null,
            null,
            "$COL_ID DESC"
        )
    }

    fun updateContact(
        id: Int,
        name: String,
        phone: String,
        relationship: String
    ): Boolean {

        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_PHONE, phone)
            put(COL_RELATIONSHIP, relationship)
        }

        val result = writableDatabase.update(
            TABLE_CONTACTS,
            values,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    fun deleteContact(id: Int): Boolean {

        val result = writableDatabase.delete(
            TABLE_CONTACTS,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    fun addPlaceNote(
        placeName: String,
        address: String,
        description: String
    ): Boolean {

        val values = ContentValues().apply {
            put(COL_PLACE_NAME, placeName)
            put(COL_ADDRESS, address)
            put(COL_DESCRIPTION, description)
            put(COL_CREATED_AT, System.currentTimeMillis().toString())
        }

        val result = writableDatabase.insert(
            TABLE_PLACE_NOTES,
            null,
            values
        )

        return result != -1L
    }

    fun getAllPlaceNotes(): android.database.Cursor {

        return readableDatabase.query(
            TABLE_PLACE_NOTES,
            null,
            null,
            null,
            null,
            null,
            "$COL_PLACE_ID DESC"
        )
    }

    fun updatePlaceNote(
        id: Int,
        placeName: String,
        address: String,
        description: String
    ): Boolean {

        val values = ContentValues().apply {
            put(COL_PLACE_NAME, placeName)
            put(COL_ADDRESS, address)
            put(COL_DESCRIPTION, description)
        }

        val result = writableDatabase.update(
            TABLE_PLACE_NOTES,
            values,
            "$COL_PLACE_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    fun deletePlaceNote(id: Int): Boolean {

        val result = writableDatabase.delete(
            TABLE_PLACE_NOTES,
            "$COL_PLACE_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    private fun createDailyTasksTable(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE $TABLE_DAILY_TASKS (
            $COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_TASK_TITLE TEXT NOT NULL,
            $COL_TASK_DESCRIPTION TEXT,
            $COL_TASK_DATE TEXT,
            $COL_TASK_COMPLETED INTEGER DEFAULT 0
        )
    """.trimIndent()

        db.execSQL(createTable)
    }

    fun addDailyTask(
        title: String,
        description: String,
        date: String
    ): Boolean {

        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_TASK_TITLE, title)
        values.put(COL_TASK_DESCRIPTION, description)
        values.put(COL_TASK_DATE, date)
        values.put(COL_TASK_COMPLETED, 0)

        val result = db.insert(TABLE_DAILY_TASKS, null, values)

        return result != -1L
    }

    fun getAllDailyTasks(): Cursor {
        val db = this.readableDatabase

        return db.rawQuery(
            "SELECT * FROM $TABLE_DAILY_TASKS ORDER BY $COL_TASK_ID DESC",
            null
        )
    }

    fun updateDailyTask(
        id: Int,
        title: String,
        description: String,
        date: String
    ): Boolean {

        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COL_TASK_TITLE, title)
        values.put(COL_TASK_DESCRIPTION, description)
        values.put(COL_TASK_DATE, date)

        val result = db.update(
            TABLE_DAILY_TASKS,
            values,
            "$COL_TASK_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    fun deleteDailyTask(id: Int): Boolean {

        val db = this.writableDatabase

        val result = db.delete(
            TABLE_DAILY_TASKS,
            "$COL_TASK_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

    fun updateTaskStatus(id: Int, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TASK_COMPLETED, if (isCompleted) 1 else 0)
        }

        val result = db.update(
            TABLE_DAILY_TASKS,
            values,
            "$COL_TASK_ID = ?",
            arrayOf(id.toString())
        )

        return result > 0
    }

}
