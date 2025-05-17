package com.medialert.medinotiapp.utils

import com.medialert.medinotiapp.models.Medication

sealed class DailyItem {
    data class Header(val title: String) : DailyItem()
    data class Med(val medication: Medication) : DailyItem()
}