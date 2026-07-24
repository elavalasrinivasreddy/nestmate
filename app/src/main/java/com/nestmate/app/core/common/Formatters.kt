package com.nestmate.app.core.common

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats rent/deposit for display: thousands-separated, no trailing decimals,
 * with a currency symbol when known (INR -> "₹"). Falls back to the raw code.
 *
 * e.g. formatMoney("INR", 26897.0) -> "₹26,897"
 */
fun formatMoney(currency: String, amount: Double): String {
    val symbol = when (currency.uppercase()) {
        "INR" -> "₹"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$currency "
    }
    val number = NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")).format(amount.toLong())
    return "$symbol$number"
}

/** Rent with the monthly suffix, e.g. "₹26,897/mo". */
fun formatRent(currency: String, amount: Double): String = "${formatMoney(currency, amount)}/mo"
