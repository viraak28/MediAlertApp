package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.models.Medication

class MedicationAdapter(
    private val medications: List<Medication>,
    private val onItemClickListener: (Medication) -> Unit // Callback para manejar clics en los items
) : RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvMedicationName)
        val dosageTextView: TextView = view.findViewById(R.id.tvMedicationDosage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medication = medications[position]
        holder.nameTextView.text = medication.name
        holder.dosageTextView.text = medication.dosage

        // Manejar clics en el elemento completo
        holder.itemView.setOnClickListener { onItemClickListener(medication) }
    }

    override fun getItemCount(): Int = medications.size
}
