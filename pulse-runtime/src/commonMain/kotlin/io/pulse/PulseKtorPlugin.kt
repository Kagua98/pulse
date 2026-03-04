package io.pulse

import io.pulse.internal.epochMillis
import io.pulse.model.HttpTransaction
import io.pulse.model.TransactionStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.save
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PulseKtorPlugin private constructor(private val config: PulseConfig) {

    companion object Plugin : HttpClientPlugin<PulseConfig, PulseKtorPlugin> {

        override val key = AttributeKey<PulseKtorPlugin>("Pulse")

        override fun prepare(block: PulseConfig.() -> Unit): PulseKtorPlugin {
            return PulseKtorPlugin(PulseConfig().apply(block))
        }

        @OptIn(ExperimentalUuidApi::class)
        override fun install(plugin: PulseKtorPlugin, scope: HttpClient) {
            val maxContentLength = plugin.config.maxContentLength

            scope.plugin(HttpSend).intercept { request ->
                if (!PulseCore.enabled) return@intercept execute(request)

                val id = Uuid.random().toString()
                val startTime = epochMillis()

                // Capture request details
                val requestBody = captureRequestBody(request.body, maxContentLength)
                val requestHeaders = captureHeaders(request)

                // Execute the request
                val originalCall = try {
                    execute(request)
                } catch (cause: Exception) {
                    PulseCore.store.addTransaction(
                        HttpTransaction(
                            id = id,
                            method = request.method.value,
                            url = request.url.buildString(),
                            host = request.url.host,
                            path = request.url.pathSegments.joinToString("/"),
                            scheme = request.url.protocol.name,
                            requestHeaders = requestHeaders,
                            requestBody = requestBody,
                            requestContentType = request.contentType()?.toString(),
                            requestSize = requestBody?.encodeToByteArray()?.size?.toLong() ?: 0L,
                            timestamp = startTime,
                            duration = epochMillis() - startTime,
                            error = cause.message ?: cause::class.simpleName ?: "Unknown error",
                            status = TransactionStatus.Failed,
                        ),
                    )
                    throw cause
                }

                // Save response so body can be read multiple times
                val savedCall = originalCall.save()
                val duration = epochMillis() - startTime

                val responseBody = captureResponseBody(savedCall, maxContentLength)
                val responseHeaders = savedCall.response.headers.entries()
                    .associate { (key, values) -> key to values.joinToString(", ") }

                PulseCore.store.addTransaction(
                    HttpTransaction(
                        id = id,
                        method = request.method.value,
                        url = request.url.buildString(),
                        host = request.url.host,
                        path = request.url.pathSegments.joinToString("/"),
                        scheme = request.url.protocol.name,
                        requestHeaders = requestHeaders,
                        requestBody = requestBody,
                        requestContentType = request.contentType()?.toString(),
                        requestSize = requestBody?.encodeToByteArray()?.size?.toLong() ?: 0L,
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
                    ),
                )

                savedCall
            }
        }

        private fun captureHeaders(request: HttpRequestBuilder): Map<String, String> {
            return request.headers.entries()
                .associate { (key, values) -> key to values.joinToString(", ") }
        }

        private fun captureRequestBody(body: Any, maxLength: Long): String? {
            return try {
                when (body) {
                    is OutgoingContent.NoContent -> null
                    is OutgoingContent.ByteArrayContent -> {
                        val bytes = body.bytes()
                        if (bytes.size <= maxLength) {
                            bytes.decodeToString()
                        } else {
                            "[Body too large: ${bytes.size} bytes]"
                        }
                    }
                    is OutgoingContent.ReadChannelContent -> "[Streaming content]"
                    is OutgoingContent.WriteChannelContent -> "[Streaming content]"
                    is OutgoingContent.ProtocolUpgrade -> "[Protocol upgrade]"
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }

        private suspend fun captureResponseBody(
            call: io.ktor.client.call.HttpClientCall,
            maxLength: Long,
        ): String? {
            return try {
                val contentLength = call.response.contentLength() ?: 0L
                if (contentLength > maxLength && contentLength > 0L) {
                    "[Body too large: $contentLength bytes]"
                } else {
                    call.response.bodyAsText()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
