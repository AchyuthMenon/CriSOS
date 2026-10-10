package com.crisos.app.sync

import android.util.Log
import com.crisos.app.domain.model.SosMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class UploadResult {
    data class Success(val id: String, val timestamp: Long) : UploadResult()
    data class ClientError(val statusCode: Int, val message: String) : UploadResult()
    data class RetryableError(val statusCode: Int?, val message: String) : UploadResult()
}

/**
 * HTTP client for uploading SOS messages to the remote MongoDB Atlas backend.
 */
class CloudApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {

    companion object {
        private const val TAG = "CloudApiClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Upload an SOS message to the remote backend at [baseUrl].
     */
    fun uploadSos(baseUrl: String, message: SosMessage): UploadResult {
        val endpointUrl = "${baseUrl.trimEnd('/')}/api/sos"

        // Build payload matching backend DTO
        val payload = mapOf(
            "id" to message.id,
            "messageType" to message.messageType,
            "senderDeviceId" to message.senderDeviceId,
            "emergencyCategory" to message.emergencyCategory.name,
            "description" to message.description,
            "assistanceTypes" to message.assistanceTypes.map { it.name },
            "latitude" to message.latitude,
            "longitude" to message.longitude,
            "locationAccuracyMeters" to message.locationAccuracyMeters,
            "locationTimestamp" to message.locationTimestamp,
            "createdAt" to message.createdAt,
            "expiresAt" to message.expiresAt,
            "hopCount" to message.hopCount,
            "maxHopCount" to message.maxHopCount,
            "deliveryStatus" to message.deliveryStatus.name,
            "direction" to message.direction.name,
            "incidentId" to message.incidentId
        )

        val jsonBody = gson.toJson(payload)
        val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .addHeader("Accept", "application/json")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                val code = response.code

                if (response.isSuccessful) {
                    try {
                        val json = gson.fromJson(responseBody, JsonObject::class.java)
                        val success = json.get("success")?.asBoolean ?: false
                        if (success) {
                            Log.i(TAG, "Successfully uploaded SOS ${message.id} to cloud backend ($code)")
                            UploadResult.Success(message.id, System.currentTimeMillis())
                        } else {
                            Log.w(TAG, "Backend returned 200 but success=false: $responseBody")
                            UploadResult.ClientError(code, responseBody)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Malformed response JSON from backend: $responseBody", e)
                        UploadResult.Success(message.id, System.currentTimeMillis())
                    }
                } else if (code in 400..499) {
                    Log.w(TAG, "Client error uploading SOS ${message.id}: HTTP $code - $responseBody")
                    UploadResult.ClientError(code, responseBody)
                } else {
                    Log.w(TAG, "Server error uploading SOS ${message.id}: HTTP $code - $responseBody")
                    UploadResult.RetryableError(code, responseBody)
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Network I/O error uploading SOS ${message.id} to $endpointUrl: ${e.message}")
            UploadResult.RetryableError(null, e.message ?: "Network error")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception uploading SOS ${message.id}", e)
            UploadResult.RetryableError(null, e.message ?: "Unexpected error")
        }
    }
}
