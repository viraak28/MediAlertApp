package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.utils.DailyItem

class DailyAdapter(private val items: List<DailyItem>,
                   private val onTakeClick: ((Medication) -> Unit)? = null,
                   private val onItemClick: ((Medication) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DailyItem.Header -> TYPE_HEADER
            is DailyItem.Med -> TYPE_MED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_meal_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_medication_daily, parent, false)
            MedViewHolder(view, onTakeClick, onItemClick)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DailyItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is DailyItem.Med -> (holder as MedViewHolder).bind(item.medication)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(title: String) {
            (itemView as TextView).text = title
        }
    }

    class MedViewHolder(
        itemView: View,
        private val onTakeClick: ((Medication) -> Unit)?,
        private val onItemClick: ((Medication) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(medication: Medication) {
            itemView.findViewById<TextView>(R.id.tvMedicationName).text = medication.name
            itemView.findViewById<TextView>(R.id.tvDosageOptions).text = "${medication.dosageQuantity} ${medication.administrationType}"
            // Botón Tomar
            itemView.findViewById<View>(R.id.btnTakeMedication).setOnClickListener {
                onTakeClick?.invoke(medication)
            }
            itemView.setOnClickListener {
                onItemClick?.invoke(medication)
            }
            itemView.findViewById<View>(R.id.btnViewMedi)?.setOnClickListener {
                onItemClick?.invoke(medication)
            }
        }

    }
}
