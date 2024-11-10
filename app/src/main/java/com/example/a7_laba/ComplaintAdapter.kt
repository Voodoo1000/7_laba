package com.example.a7_laba

import Complaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplaintAdapter(
    private val complaintList: MutableList<Complaint>,
    private val onComplaintClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION // Выбранная позиция элемента

    // ViewHolder для удержания элементов представления жалобы
    inner class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val surname: TextView = itemView.findViewById(R.id.surname)
        val description: TextView = itemView.findViewById(R.id.description)
        val address: TextView = itemView.findViewById(R.id.address)
        private val editButton: Button = itemView.findViewById(R.id.edit_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)

        // Слушатели нажатий на элемент (позиция элемента)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onComplaintClick(position)
                }
            }

            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(position)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }
    }

    // Метод для создания нового ViewHolder для элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.complaint_item, parent, false)
        return ComplaintViewHolder(itemView)
    }

    // Метод для привязки данных к ViewHolder
    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaintList[position]
        holder.surname.text = "Фамилия: ${complaint.lastName}"
        holder.description.text = "Описание: ${complaint.complaintText}"
        holder.address.text = "Адрес: ${complaint.address}"

        holder.itemView.setBackgroundColor(
            if (position == selectedItemPosition) {
                holder.itemView.context.getColor(R.color.selected_item)
            } else {
                holder.itemView.context.getColor(R.color.default_item)
            }
        )
    }

    override fun getItemCount(): Int {
        return complaintList.size
    }

    // Метод для установки выбранного элемента
    fun setSelectedItem(position: Int) {
        val previousItemPosition = selectedItemPosition
        selectedItemPosition = position
        notifyItemChanged(previousItemPosition) // Обновляем предыдущий выбранный элемент
        notifyItemChanged(selectedItemPosition) // Обновляем текущий выбранный элемент
    }

    // Логика изменения элемента
    fun editItem(position: Int, updatedComplaint: Complaint) {
        complaintList[position] = updatedComplaint // Заменяем элемент на обновленный
        notifyItemChanged(position) // Обновляем измененный элемент
    }
}
