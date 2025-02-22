package com.medialert.medinotiapp.models

class MedicationRepository {
    private val medications = mutableListOf<Medication>()

    fun addMedication(medication: Medication) {
        medications.add(medication)
    }

    fun getAllMedications(): List<Medication> = medications
}
