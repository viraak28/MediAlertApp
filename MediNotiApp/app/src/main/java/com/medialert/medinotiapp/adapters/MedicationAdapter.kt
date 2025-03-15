package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.databinding.ItemMedicationBinding
import com.medialert.medinotiapp.models.Medication

class MedicationAdapter(
    private val medications: MutableList<Medication>,
    private val onTakeClick: (Medication) -> Unit,
    private val onEditClick: (Medication) -> Unit,
    private val onItemClick: (Medication) -> Unit,
    private val onDeleteClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.tvMedicationName.text = "NOMBRE: ${medication.name}"
          //  binding.tvMedicationDosage.text = "Dosis: ${medication.dosage}"
         //   binding.tvMedicationFrequency.text = "Frecuencia: ${medication.frequency}"
            binding.tvDosageOptions.text = "CANTIDAD: ${medication.dosageQuantity} ${medication.administrationType}"

            binding.btnTakeMedication.setOnClickListener { onTakeClick(medication) }
            binding.btnEditMedication.setOnClickListener { onEditClick(medication) }
            binding.root.setOnClickListener { onItemClick(medication) }
            binding.btnDelete.setOnClickListener { onDeleteClick(medication) }

            val breakfastState = medication.breakfast
            val midMorningState = medication.midMorning
            val lunchState = medication.lunch
            val snackingState = medication.snacking
            val dinnerState = medication.dinner
            var switchStates = ""

            if (breakfastState) switchStates += "DESAYUNO, "
            if (midMorningState) switchStates += "MEDIA MAÑANA, "
            if (lunchState) switchStates += "COMIDA, "
            if (snackingState) switchStates += "MERIENDA, "
            if (dinnerState) switchStates += "CENA"

// si el ultimo no es la cena hay que eliminar la coma
            if (switchStates.isNotEmpty() && !dinnerState) {
                switchStates = switchStates.substring(0, switchStates.length - 2)
            }

            binding.tvMealsOptions.text="TOMAS: $switchStates"

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
