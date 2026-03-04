package io.pulse.internal

import io.pulse.model.HttpTransaction

internal actual fun generateTransactionsPdf(
    context: ShareContext,
    transactions: List<HttpTransaction>,
): String? {
    // PDF generation is not supported on native
    return null
}
