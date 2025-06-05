package com.medialert.medinotiapp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.medialert.medinotiapp.R

object TooltipHelper {
//        fun showCustomTooltip(
//            context: Context,
//            anchorView: View,
//            text: String,
//            textSize: Float = 16f,
//            backgroundColor: Int = Color.BLACK,
//            textColor: Int = Color.WHITE
//        ) {
//            val inflater = LayoutInflater.from(context)
//            val layout = inflater.inflate(R.layout.tooltip_custom, null)
//
//            // txt and style
//            val textView = layout.findViewById<TextView>(R.id.txtTooltip).apply {
//                this.text = text
//                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
//                setTextColor(textColor)
//            }
//
//            // background
//            layout.background = ContextCompat.getDrawable(context, R.drawable.bg_tooltip)
//                ?.apply { setTint(backgroundColor) }
//
//            val popupWindow = PopupWindow(
//                layout,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                true
//            )
//            //popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_tooltip))
//            // Posicion segun btn
//            val location = IntArray(2)
//            anchorView.getLocationOnScreen(location)
//            val x = location[0] + (anchorView.width / 2)
//            val y = location[1] - layout.height - 16 // 16dp arriba
//
//            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
//        }

    fun showTooltipDialog(
        context: Context,
        anchorView: View,
        text: String,
        textSize: Float = 22f,
        backgroundColor: Int = Color.BLACK,
        textColor: Int = Color.WHITE,
        duration: Long = 5000L
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltipdialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textView = dialog.findViewById<TextView>(R.id.txtTooltip)
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        textView.setTextColor(textColor)

        val layout = dialog.findViewById<LinearLayout>(R.id.tooltipRoot)
        layout?.background = ContextCompat.getDrawable(context, R.drawable.bg_tooltip)
            ?.apply { setTint(backgroundColor) }

        // Calcula la posición y tamaño del botón
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val params = dialog.window?.attributes
        params?.width = anchorView.width
        params?.height = anchorView.height
        params?.x = location[0]
        params?.y = location[1]
        params?.gravity = Gravity.TOP or Gravity.START
        dialog.window?.attributes = params

        dialog.setCancelable(true)
        dialog.show()

        // Cierre automático después de 'duration' milisegundos
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, duration)
    }

}