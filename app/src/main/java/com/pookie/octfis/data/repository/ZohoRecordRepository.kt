package com.pookie.octfis.data.repository

import com.pookie.octfis.data.remote.ZohoApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZohoRecordRepository @Inject constructor(
    private val api: ZohoApiService,
) {

    suspend fun createRecord(
        module: String,
        payload: Map<String, Any>,
    ): Result<String> = try {
        val body   = mapOf("data" to listOf(payload))
        val result = api.createRecord(module, body).data?.firstOrNull()
        if (result?.status == "success" && result.details?.id != null)
            Result.success(result.details.id)
        else
            Result.failure(Exception(result?.message ?: "Create failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateRecord(
        module: String,
        id: String,
        payload: Map<String, Any>,
    ): Result<String> = try {
        val body   = mapOf("data" to listOf(payload))
        val result = api.updateRecord(module, id, body).data?.firstOrNull()
        if (result?.status == "success")
            Result.success(result.details?.id ?: id)
        else
            Result.failure(Exception(result?.message ?: "Update failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRecord(
        module: String,
        id: String,
    ): Result<Map<String, Any>> = try {
        val response = api.getRecord(module, id)
        @Suppress("UNCHECKED_CAST")
        val data = (response["data"] as? List<*>)
            ?.firstOrNull() as? Map<String, Any>
            ?: response
        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteRecord(
        module: String,
        id: String,
    ): Result<Unit> = try {
        api.deleteRecord(module, id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}