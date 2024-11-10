package com.example.a7_laba

import Complaint
import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etLastName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etComplaintText: EditText
    private lateinit var rgHousingType: RadioGroup
    private lateinit var btnSubmit: Button
    private lateinit var gestureDetector: GestureDetectorCompat
    private val complaintList = mutableListOf<Complaint>()
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Устанавливаем Toolbar как ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        databaseHelper = DatabaseHelper(this)

        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etAddress = findViewById(R.id.etAddress)
        etComplaintText = findViewById(R.id.etComplaint)
        rgHousingType = findViewById(R.id.rgHousingType)
        btnSubmit = findViewById(R.id.btnSubmit)

        val complaintToEdit = intent.getParcelableExtra<Complaint>("complaintToEdit")
        val isEditMode = complaintToEdit != null

        if (isEditMode) {
            etLastName.setText(complaintToEdit?.lastName)
            etFirstName.setText(complaintToEdit?.firstName)
            etMiddleName.setText(complaintToEdit?.middleName)
            etAddress.setText(complaintToEdit?.address)
            etComplaintText.setText(complaintToEdit?.complaintText)

            when (complaintToEdit?.housingType) {
                "Квартира" -> rgHousingType.check(R.id.rbApartment)
                "Дом" -> rgHousingType.check(R.id.rbHouse)
            }
        }

        btnSubmit.setOnClickListener {
            submitForm(isEditMode, complaintToEdit)
        }

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener(isEditMode, complaintToEdit))
        btnSubmit.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun submitForm(isEditMode: Boolean, complaintToEdit: Complaint?) {
        val lastName = etLastName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val complaintText = etComplaintText.text.toString().trim()

        if (lastName.isEmpty() || firstName.isEmpty() || address.isEmpty() || complaintText.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val housingType = when (rgHousingType.checkedRadioButtonId) {
            R.id.rbApartment -> "Квартира"
            R.id.rbHouse -> "Дом"
            else -> "Неизвестно"
        }

        // Если пользователь еще не существует, создаем его
        val userId = databaseHelper.addUser(lastName, firstName, middleName)

        val updatedComplaint = if (isEditMode && complaintToEdit != null) {
            Complaint(complaintToEdit.id, lastName, firstName, middleName, housingType, address, complaintText)
        } else {
            // Если создаем новый, id = 0
            Complaint(0, lastName, firstName, middleName, housingType, address, complaintText)
        }

        if (isEditMode) {
            val resultIntent = Intent()
            resultIntent.putExtra("updatedComplaint", updatedComplaint as Parcelable)  // Передаем саму жалобу
            resultIntent.putExtra("editPosition", intent.getIntExtra("editPosition", -1)) // Передаем позицию для обновления в списке
            resultIntent.putExtra("complaintId", updatedComplaint.id)  // Передаем ID жалобы
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            // Добавляем жалобу, передавая ID пользователя
            val complaintId = databaseHelper.addComplaint(updatedComplaint, userId.toInt())
            if (complaintId != -1L) {
                updatedComplaint.id = complaintId.toInt()  // Преобразуем Long в Int
                complaintList.add(updatedComplaint)  // Добавляем в список
                etLastName.text.clear()
                etFirstName.text.clear()
                etMiddleName.text.clear()
                etAddress.text.clear()
                etComplaintText.text.clear()
                rgHousingType.clearCheck()
            } else {
                Toast.makeText(this, "Ошибка при добавлении жалобы", Toast.LENGTH_SHORT).show()
            }
        }
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_second_activity -> {
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)

        }
        return super.onOptionsItemSelected(item)
    }
    private inner class SwipeGestureListener(val isEditMode: Boolean, val complaintToEdit: Complaint?) :
        GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2?.x?.minus(e1!!.x) ?: 0.0f
            val diffY = e2?.y?.minus(e1!!.y) ?: 0.0f
            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    submitForm(isEditMode, complaintToEdit)
                }
                return true
            }
            return false
        }
    }
}
