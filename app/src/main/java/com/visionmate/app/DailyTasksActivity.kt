package com.visionmate.app

import android.app.Activity
import android.os.Bundle
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import android.graphics.Color
import com.visionmate.app.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher

class DailyTasksActivity : Activity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tasksContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var addTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_tasks)

        databaseHelper = DatabaseHelper(this)

        tasksContainer = findViewById(R.id.tasksContainer)
        searchInput = findViewById(R.id.etSearchTask)
        addTaskButton = findViewById(R.id.btnAddTask)

        addTaskButton.setOnClickListener {
            showTaskDialog()
        }

        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                loadTasks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        loadTasks()
    }

    // ADD NEW TASK

    private fun showTaskDialog() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val titleInput = EditText(this).apply {
            hint = "Enter task title"
        }

        val descriptionInput = EditText(this).apply {
            hint = "Enter task description"
            minLines = 2
            gravity = Gravity.TOP
        }

        val dateButton = Button(this).apply {
            text = "Select Task Date"
        }

        var selectedDate = ""

        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            android.app.DatePickerDialog(
                this,
                { _, year, month, day ->

                    selectedDate = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                    dateButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        layout.addView(titleInput)
        layout.addView(descriptionInput)
        layout.addView(dateButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(layout)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title = titleInput.text.toString().trim()
                    val description =
                        descriptionInput.text.toString().trim()

                    if (title.isEmpty()) {
                        titleInput.error = "Task title is required"
                        return@setOnClickListener
                    }

                    val date = if (selectedDate.isEmpty()) {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(Calendar.getInstance().time)
                    } else {
                        selectedDate
                    }

                    val success = databaseHelper.addDailyTask(
                        title,
                        description,
                        date
                    )

                    if (success) {

                        Toast.makeText(
                            this,
                            "Task added successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadTasks()
                        dialog.dismiss()

                    } else {

                        Toast.makeText(
                            this,
                            "Unable to add task",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    // DISPLAY ALL TASKS


    private fun loadTasks(query: String=" ") {

        tasksContainer.removeAllViews()

        val cursor = databaseHelper.getAllDailyTasks()

        while (cursor.moveToNext()) {

            val id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
            )

            val title = cursor.getString(
                cursor.getColumnIndexOrThrow("task_title")
            )

            val description = cursor.getString(
                cursor.getColumnIndexOrThrow("task_description")
            )

            val date = cursor.getString(
                cursor.getColumnIndexOrThrow("task_date")
            )

            if (
                !title.contains(query.trim(), ignoreCase = true) &&
                !description.contains(query.trim(), ignoreCase = true)
            ) {
                continue
            }

            val isCompleted = cursor.getInt(
                cursor.getColumnIndexOrThrow("is_completed")
            ) == 1

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                setBackgroundColor(Color.WHITE)
            }

            val status = if (isCompleted) "Completed" else "Pending"

            val taskDetails = TextView(this).apply {
                text = """
                Task: $title
                Description: $description
                Date: $date
                Status: $status
            """.trimIndent()

                textSize = 17f
                setTextColor(Color.BLACK)
                setPadding(8, 8, 8, 16)
            }

            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val statusButton = Button(this).apply {
                text = if (isCompleted) {
                    "Mark Pending"
                } else {
                    "Mark Completed"
                }

                setOnClickListener {

                    val success = databaseHelper.updateTaskStatus(
                        id,
                        !isCompleted
                    )

                    if (success) {
                        Toast.makeText(
                            this@DailyTasksActivity,
                            if (isCompleted) {
                                "Task marked as pending"
                            } else {
                                "Task completed successfully"
                            },
                            Toast.LENGTH_SHORT
                        ).show()

                        loadTasks()
                    }
                }
            }

            val editButton = Button(this).apply {
                text = "Edit"

                setOnClickListener {
                    showEditTaskDialog(
                        id,
                        title,
                        description,
                        date
                    )
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"

                setOnClickListener {

                    AlertDialog.Builder(this@DailyTasksActivity)
                        .setTitle("Delete Task")
                        .setMessage(
                            "Are you sure you want to delete this task?"
                        )
                        .setPositiveButton("Delete") { _, _ ->

                            val success =
                                databaseHelper.deleteDailyTask(id)

                            if (success) {
                                Toast.makeText(
                                    this@DailyTasksActivity,
                                    "Task deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadTasks()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            actions.addView(statusButton)
            actions.addView(editButton)
            actions.addView(deleteButton)

            card.addView(taskDetails)
            card.addView(actions)

            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            cardParams.setMargins(0, 0, 0, 20)

            tasksContainer.addView(card, cardParams)
        }

        cursor.close()
    }


    // EDIT EXISTING TASK

    private fun showEditTaskDialog(
        id: Int,
        oldTitle: String,
        oldDescription: String,
        oldDate: String
    ) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val titleInput = EditText(this).apply {
            hint = "Enter task title"
            setText(oldTitle)
        }

        val descriptionInput = EditText(this).apply {
            hint = "Enter task description"
            setText(oldDescription)
            minLines = 2
            gravity = Gravity.TOP
        }

        val dateButton = Button(this).apply {
            text = oldDate
        }

        var selectedDate = oldDate

        dateButton.setOnClickListener {

            val calendar = Calendar.getInstance()

            android.app.DatePickerDialog(
                this,
                { _, year, month, day ->

                    selectedDate = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )

                    dateButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        layout.addView(titleInput)
        layout.addView(descriptionInput)
        layout.addView(dateButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(layout)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title = titleInput.text.toString().trim()

                    val description =
                        descriptionInput.text.toString().trim()

                    if (title.isEmpty()) {
                        titleInput.error = "Task title is required"
                        return@setOnClickListener
                    }

                    val success = databaseHelper.updateDailyTask(
                        id,
                        title,
                        description,
                        selectedDate
                    )

                    if (success) {

                        Toast.makeText(
                            this,
                            "Task updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadTasks()
                        dialog.dismiss()

                    } else {

                        Toast.makeText(
                            this,
                            "Unable to update task",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}
