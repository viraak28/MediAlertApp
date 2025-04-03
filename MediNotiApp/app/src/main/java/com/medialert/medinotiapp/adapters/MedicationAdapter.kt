package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.databinding.ItemMedicationBinding
import com.medialert.medinotiapp.models.Medication

open class MedicationAdapter(
    private val medications: MutableList<Medication>,
    private val onTakeClick: ((Medication) -> Unit)?,
    private val onEditClick: ((Medication) -> Unit)?,
    private val onItemClick: ((Medication) -> Unit)?,
    private val onDeleteClick: ((Medication) -> Unit)?,
    private val isDailyView: Boolean = false // Nueva bandera para controlar el diseño
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.tvMedicationName.text = "NOMBRE: ${medication.name}"
            binding.tvDosageOptions.text = "CANTIDAD: ${medication.dosageQuantity} ${medication.administrationType}"

            // Mostrar u ocultar botones según el modo (vista diaria o completa)
            if (isDailyView) {
                binding.btnTakeMedication.visibility = View.GONE
                binding.btnEditMedication.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE

                // Ajustar dinámicamente el tamaño de la CardView para vista diaria
                val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT // Ajustar altura automática
                params.topMargin = 8 // Reducir margen superior
                params.bottomMargin = 8 // Reducir margen inferior
                binding.root.layoutParams = params

                binding.root.radius = 4f // Reducir el radio de las esquinas
                binding.root.cardElevation = 2f // Reducir la elevación de la CardView
            } else {
                binding.btnTakeMedication.visibility = if (onTakeClick != null) View.VISIBLE else View.GONE
                binding.btnEditMedication.visibility = if (onEditClick != null) View.VISIBLE else View.GONE
                binding.btnDelete.visibility = if (onDeleteClick != null) View.VISIBLE else View.GONE

                // Restaurar tamaño original de la CardView para vista completa
                val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT // Mantener altura automática
                params.topMargin = 16 // Márgenes originales
                params.bottomMargin = 16 // Márgenes originales
                binding.root.layoutParams = params

                binding.root.radius = 8f // Restaurar el radio de las esquinas
                binding.root.cardElevation = 4f // Restaurar la elevación de la CardView
            }

            // Configurar acciones solo si están habilitadas:
            binding.btnTakeMedication.setOnClickListener { onTakeClick?.invoke(medication) }
            binding.btnEditMedication.setOnClickListener { onEditClick?.invoke(medication) }
            binding.root.setOnClickListener { onItemClick?.invoke(medication) }
            binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(medication) }

            // Configurar las tomas del día:
            val switchStates = buildString {
                if (medication.breakfast) append("DESAYUNO, ")
                if (medication.midMorning) append("MEDIA MAÑANA, ")
                if (medication.lunch) append("COMIDA, ")
                if (medication.snacking) append("MERIENDA, ")
                if (medication.dinner) append("CENA")
            }.trimEnd(',', ' ')

            binding.tvMealsOptions.text = "TOMAS: $switchStates"
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
