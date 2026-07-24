package com.nestmate.app.core.common

import android.content.Context
import android.content.Intent
import com.nestmate.app.data.model.Listing

/** Builds the shareable blurb for a room listing. */
fun listingShareText(listing: Listing): String = buildString {
    append(listing.title)
    append('\n')
    append(formatRent(listing.currency, listing.rentAmount))
    append('\n')
    append("${listing.location.area}, ${listing.location.city}")
    append("\n\n")
    append("Shared via Nestmate")
}

/** Launches the system share sheet (WhatsApp, etc.) with plain text. */
fun sharePlainText(context: Context, text: String, chooserTitle: String = "Share") {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
