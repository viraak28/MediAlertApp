package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.databinding.ItemMedicationBinding
import com.medialert.medinotiapp.models.Medication

class MedicationAdapter(
    private val medications: MutableList<Medication>,
    private val onTakeClick: (Medication) -> Unit,
    private val onEditClick: (Medication) -> Unit,
    private val onItemClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.tvMedicationName.text = medication.name
            binding.tvMedicationDosage.text = "Dosis: ${medication.dosage}"
            binding.tvMedicationFrequency.text = "Frecuencia: ${medication.frequency}"
            binding.btnTakeMedication.setOnClickListener { onTakeClick(medication) }
            binding.btnEditMedication.setOnClickListener { onEditClick(medication) }
            binding.root.setOnClickListener { onItemClick(medication) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(medications[position])
    }

    override fun getItemCount(): Int = medications.size
}
