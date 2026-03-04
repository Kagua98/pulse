package io.pulse.internal

import io.pulse.Pulse
import io.pulse.model.HttpTransaction
import io.pulse.model.TransactionStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.save
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength
import io.ktor.http.contentType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal suspend fun replayRequest(transaction: HttpTransaction): HttpTransaction {
    val id = Uuid.random().toString()
    val startTime = epochMillis()

    // Create a temporary client WITHOUT the Pulse plugin to avoid infinite recording loop
    val tempClient = HttpClient()

    return try {
        val response = tempClient.request(transaction.url) {
            method = HttpMethod.parse(transaction.method)

            // Copy request headers (skip content-related headers that Ktor manages)
            headers {
                transaction.requestHeaders.forEach { (key, value) ->
                    val lowerKey = key.lowercase()
                    if (lowerKey != "content-length" && lowerKey != "host") {
                        append(key, value)
                    }
                }
            }

            // Set request body if present
            if (transaction.requestBody != null) {
                setBody(transaction.requestBody)
            }
        }

        val savedCall = response.call.save()
        val duration = epochMillis() - startTime
        val responseBody = try {
            savedCall.response.bodyAsText()
        } catch (_: Exception) {
            null
        }

        val responseHeaders = savedCall.response.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }

        val replayedTransaction = HttpTransaction(
            id = id,
            method = transaction.method,
            url = transaction.url,
            host = transaction.host,
            path = transaction.path,
            scheme = transaction.scheme,
            requestHeaders = transaction.requestHeaders,
            requestBody = transaction.requestBody,
            requestContentType = transaction.requestContentType,
            requestSize = transaction.requestSize,
            responseCode = savedCall.response.status.value,
            responseMessage = savedCall.response.status.description,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            responseContentType = savedCall.response.contentType()?.toString(),
            responseSize = responseBody?.encodeToByteArray()?.size?.toLong()
                ?: savedCall.response.contentLength() ?: 0L,
            duration = duration,
            timestamp = startTime,
            status = TransactionStatus.Complete,
        )

        // Record the replayed transaction in Pulse's store
        Pulse.store.addTransaction(replayedTransaction)

        replayedTransaction
    } catch (cause: Exception) {
        val failedTransaction = HttpTransaction(
            id = id,
            method = transaction.method,
            url = transaction.url,
            host = transaction.host,
            path = transaction.path,
            scheme = transaction.scheme,
            requestHeaders = transaction.requestHeaders,
            requestBody = transaction.requestBody,
            requestContentType = transaction.requestContentType,
            requestSize = transaction.requestSize,
            timestamp = startTime,
            duration = epochMillis() - startTime,
            error = cause.message ?: cause::class.simpleName ?: "Unknown error",
            status = TransactionStatus.Failed,
        )

        // Record the failed replayed transaction too
        Pulse.store.addTransaction(failedTransaction)

        failedTransaction
    } finally {
        tempClient.close()
    }
}
