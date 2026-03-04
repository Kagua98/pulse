package io.pulse.internal

import io.pulse.model.HttpTransaction

internal expect fun generateTransactionsPdf(
    context: ShareContext,
    transactions: List<HttpTransaction>,
): String?
