package com.example.takupdf

import java.util.*;
import android.text.format.DateFormat

object Methods {
    fun formatTimestamp(timestamp: Long): String {
        val calender = Calendar.getInstance(Locale.ENGLISH)
        calender.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy",calender).toString()
    }
}