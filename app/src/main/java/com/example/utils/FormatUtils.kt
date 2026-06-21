package com.example.utils

import java.text.NumberFormat
import java.util.*

object FormatUtils {

    fun formatPrice(price: Double): String {
        if (price <= 0.0) return "—"
        return when {
            price < 0.0001 -> String.format(Locale.US, "%.8f", price)
            price < 0.01 -> String.format(Locale.US, "%.6f", price)
            price < 1.0 -> String.format(Locale.US, "%.5f", price)
            price < 100.0 -> {
                val nf = NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 4
                }
                nf.format(price)
            }
            else -> {
                val nf = NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                nf.format(price)
            }
        }
    }

    fun formatVolume(vol: Double): String {
        if (vol <= 0.0) return "$0"
        return when {
            vol >= 1_000_000_000.0 -> String.format(Locale.US, "$%.2fB", vol / 1_000_000_000.0)
            vol >= 1_000_000.0 -> String.format(Locale.US, "$%.2fM", vol / 1_000_000.0)
            vol >= 1_000.0 -> String.format(Locale.US, "$%.2fK", vol / 1_000.0)
            else -> String.format(Locale.US, "$%.0f", vol)
        }
    }

    fun cleanSymbol(symbol: String): String {
        return symbol.replace("USDT", "").replace("-USDT", "").replace("-USD", "")
    }
}
