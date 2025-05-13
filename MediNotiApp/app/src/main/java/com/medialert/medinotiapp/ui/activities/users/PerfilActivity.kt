package com.medialert.medinotiapp.ui.activities.users

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityPerfilBinding
import com.medialert.medinotiapp.models.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var medinotiappDatabase: MedinotiappDatabase
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)
        userId = intent.getIntExtra("USUARIO_ID", -1)

        loadUserData()
        setupExportButton()
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = medinotiappDatabase.userDao().getUserById(userId)
            if (usuario != null) {
                withContext(Dispatchers.Main) {
                    binding.tvNombre.text = usuario.nombre
                    binding.tvApellido.text = usuario.apellido
                    binding.tvCorreo.text = usuario.correo
                }
            }
        }
    }

    private fun setupExportButton() {
        binding.btnExportCalendar.setOnClickListener {
            lifecycleScope.launch {
                exportMedicationCalendar()
            }
        }
    }

    private suspend fun exportMedicationCalendar() {
        withContext(Dispatchers.IO) {
            try {
                // OBTENER MEDICAMENTOS usando Flow
                val medications = medinotiappDatabase.medicationDao().getMedicationsByUser(userId).first()

                // Crear PDF
                val pdfFile = createPdfFile(medications)

                // Compartir archivo
                withContext(Dispatchers.Main) {
                    sharePdfFile(pdfFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PerfilActivity,
                        "Error al exportar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun createPdfFile(medications: List<Medication>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Título
        paint.color = Color.BLACK
        paint.textSize = 18f
        canvas.drawText("CALENDARIO DE MEDICAMENTOS", 50f, 50f, paint)

        // Información del usuario
        paint.textSize = 12f
        canvas.drawText("Paciente: ${binding.tvNombre.text} ${binding.tvApellido.text}", 50f, 80f, paint)
        canvas.drawText("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", 50f, 100f, paint)

        // Tabla de medicamentos
        var yPosition = 150f
        val lineHeight = 30f

        // Encabezados
        paint.isFakeBoldText = true
        canvas.drawText("MEDICAMENTO", 50f, yPosition, paint)
        canvas.drawText("DOSIS", 200f, yPosition, paint)
        canvas.drawText("HORARIO", 350f, yPosition, paint)
        paint.isFakeBoldText = false

        // Línea separadora
        yPosition += 10f
        canvas.drawLine(50f, yPosition, 550f, yPosition, paint)
        yPosition += 20f

        // Datos de medicamentos
        for (medication in medications) {
            canvas.drawText(medication.name, 50f, yPosition, paint)
            canvas.drawText("${medication.dosageQuantity} ${medication.administrationType}", 200f, yPosition, paint)

            val timeSlots = StringBuilder()
            if (medication.breakfast) timeSlots.append("Desayuno, ")
            if (medication.midMorning) timeSlots.append("Media Mañana, ")
            if (medication.lunch) timeSlots.append("Comida, ")
            if (medication.snacking) timeSlots.append("Merienda, ")
            if (medication.dinner) timeSlots.append("Cena, ")

            val times = if (timeSlots.isNotEmpty()) {
                timeSlots.substring(0, timeSlots.length - 2)
            } else {
                "Sin horario"
            }

            canvas.drawText(times, 350f, yPosition, paint)
            yPosition += lineHeight

            // Si vamos a sobrepasar la página, crear una nueva
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                val newPage = pdfDocument.startPage(newPageInfo)
                yPosition = 50f
            }
        }

        pdfDocument.finishPage(page)

        // Guardar PDF
        val fileName = "Medicamentos_${binding.tvNombre.text}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf"
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        FileOutputStream(filePath).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return filePath
    }

    private fun sharePdfFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Calendario de Medicamentos")
            putExtra(
                Intent.EXTRA_TEXT,
                "Adjunto el calendario de medicamentos para ${binding.tvNombre.text} ${binding.tvApellido.text}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartir Calendario de Medicamentos"))

        Toast.makeText(
            this,
            "Calendario generado correctamente",
            Toast.LENGTH_SHORT
        ).show()
    }
}
