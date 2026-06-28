package com.tupiekozirki.megakazino

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Long.toRubles(): String {
    val rubles = this / 100
    val symbols =
        DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' '
        }
    return "${DecimalFormat("#,###", symbols).format(rubles)} ₽"
}
