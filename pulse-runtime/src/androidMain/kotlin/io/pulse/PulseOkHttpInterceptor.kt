package io.pulse

import io.pulse.internal.epochMillis
import io.pulse.model.HttpTransaction
import io.pulse.model.TransactionStatus
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * An OkHttp [Interceptor] that automatically records HTTP transactions into Pulse.
 *
 * Add this interceptor to your `OkHttpClient` to capture network traffic:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(PulseOkHttpInterceptor())
 *     .build()
 * ```
 *
 * The interceptor respects [Pulse.enabled] -- when Pulse is disabled, requests pass
 * through without any overhead.
 */
class PulseOkHttpInterceptor : Interceptor {

    @OptIn(ExperimentalUuidApi::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!Pulse.enabled) return chain.proceed(chain.request())

        val request = chain.request()
        val startTime = epochMillis()

        val requestBody = request.body?.let { body ->
            try {
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (_: Exception) {
                null
            }
        }

        val requestHeaders = buildMap {
            val headers = request.headers
            for (i in 0 until headers.size) {
                put(headers.name(i), headers.value(i))
            }
        }

        val transaction = HttpTransaction(
            id = Uuid.random().toString(),
            url = request.url.toString(),
            host = request.url.host,
            path = "/" + request.url.encodedPathSegments.joinToString("/"),
            scheme = request.url.scheme,
            method = request.method,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            requestContentType = request.body?.contentType()?.toString(),
            requestSize = request.body?.contentLength() ?: 0L,
            timestamp = startTime,
            status = TransactionStatus.Requested,
        )

        // Record the in-flight transaction so it appears immediately in the UI
        Pulse.store.addTransaction(transaction)

        return try {
            val response = chain.proceed(request)
            val responseBodyString = response.peekBody(Long.MAX_VALUE).string()
            val duration = epochMillis() - startTime

            val responseHeaders = buildMap {
                val headers = response.headers
                for (i in 0 until headers.size) {
                    put(headers.name(i), headers.value(i))
                }
            }

            val completedTransaction = transaction.copy(
                responseCode = response.code,
                responseMessage = response.message,
                responseHeaders = responseHeaders,
                responseBody = responseBodyString,
                responseContentType = response.body?.contentType()?.toString(),
                responseSize = responseBodyString.length.toLong(),
                duration = duration,
                status = TransactionStatus.Complete,
            )

            Pulse.store.updateTransaction(transaction.id) { completedTransaction }
            response
        } catch (e: Exception) {
            val failedTransaction = transaction.copy(
                error = e.message ?: e.toString(),
                duration = epochMillis() - startTime,
                status = TransactionStatus.Failed,
            )
            Pulse.store.updateTransaction(transaction.id) { failedTransaction }
            throw e
        }
    }
}
