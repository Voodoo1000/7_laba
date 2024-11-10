package com.example.a7_laba

import Complaint
import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SecondActivity : AppCompatActivity() {
    private lateinit var rvComplaintList: RecyclerView
    private lateinit var complaintAdapter: ComplaintAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private val complaintList = mutableListOf<Complaint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Инициализация RecyclerView и установка GridLayout
        rvComplaintList = findViewById(R.id.complaintList)
        rvComplaintList.layoutManager = GridLayoutManager(this, 2)

        // Устанавливаем Toolbar как ActionBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Инициализация базы данных
        databaseHelper = DatabaseHelper(this)

        // Инициализация адаптера и подключение к RecyclerView
        complaintAdapter = ComplaintAdapter(
            complaintList,
            { position -> complaintAdapter.setSelectedItem(position) }, // Для выделения
            { position -> editItem(position) }, // Для редактирования
            { position -> deleteItem(position) } // Для удаления
        )
        rvComplaintList.adapter = complaintAdapter

        // Загружаем жалобы из базы данных
        loadComplaintsFromDatabase()

        // Настройка ItemTouchHelper для обработки свайпов вверх и удаления элементов
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                when (direction) {
                    ItemTouchHelper.UP -> {
                        // Открытие главной активности
                        val intent = Intent(this@SecondActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvComplaintList)
    }

    // Метод для загрузки всех жалоб из базы данных
    private fun loadComplaintsFromDatabase() {
        // Получаем все жалобы из базы данных
        val complaintsFromDb = databaseHelper.getAllComplaints()
        if (complaintsFromDb.isNotEmpty()) {
            complaintList.addAll(complaintsFromDb)
            complaintAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Жалобы не найдены в базе данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editItem(position: Int) {
        val complaint = complaintList[position]
        val intent = Intent(this, MainActivity::class.java)

        // Передаем жалобу для редактирования
        intent.putExtra("complaintToEdit", complaint as Parcelable)
        intent.putExtra("editPosition", position)
        startActivityForResult(intent, REQUEST_EDIT)
    }

    private fun deleteItem(position: Int) {
        val complaint = complaintList[position]

        // Используем id для удаления из базы данных
        val isDeleted = databaseHelper.deleteComplaint(complaint.id)

        if (isDeleted) {
            // Удаляем жалобу из списка и обновляем адаптер
            complaintList.removeAt(position)
            complaintAdapter.notifyItemRemoved(position)
        } else {
            Toast.makeText(this, "Ошибка при удалении жалобы", Toast.LENGTH_SHORT).show()
        }
    }

    // В SecondActivity на обработку ответа от MainActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            val updatedComplaint = data?.getParcelableExtra<Complaint>("updatedComplaint")
            val editPosition = data?.getIntExtra("editPosition", -1) ?: -1
            val complaintId = data?.getIntExtra("complaintId", -1) ?: -1  // Получаем ID жалобы

            if (updatedComplaint != null && editPosition != -1 && complaintId != -1) {
                // Обновляем данные в базе данных с использованием ID
                updatedComplaint.id = complaintId  // Обновляем ID в объекте жалобы
                val isUpdated = databaseHelper.updateComplaint(updatedComplaint)

                if (isUpdated) {
                    // Обновляем элемент в адаптере
                    complaintAdapter.editItem(editPosition, updatedComplaint)
                } else {
                    Toast.makeText(this, "Ошибка при обновлении жалобы", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    companion object {
        const val REQUEST_EDIT = 1
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_second_activity -> {
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }
}
