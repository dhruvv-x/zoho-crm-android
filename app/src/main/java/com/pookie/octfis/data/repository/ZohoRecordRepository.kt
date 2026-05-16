// data/repository/ZohoRecordRepository.kt
package com.pookie.octfis.data.repository

import com.pookie.octfis.data.remote.ZohoApiService
import javax.inject.Inject
import javax.inject.Singleton

/** Raw record from a generic list — id + all field values as strings/any. */
data class RawRecord(
    val id    : String,
    val fields: Map<String, Any?>,
)

@Singleton
class ZohoRecordRepository @Inject constructor(
    private val api: ZohoApiService,
) {

    // ── List ──────────────────────────────────────────────────────────────────

    suspend fun listRecords(
        module   : String,
        page     : Int    = 1,
        perPage  : Int    = 50,
        sortBy   : String = "Modified_Time",
        sortOrder: String = "desc",
    ): Result<Pair<List<RawRecord>, Boolean>> = try {
        val response = api.listRecords(module, page, perPage, sortBy, sortOrder)

        @Suppress("UNCHECKED_CAST")
        val dataList = response["data"] as? List<Map<String, Any?>> ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val info    = response["info"] as? Map<String, Any?>
        val hasMore = (info?.get("more_records") as? Boolean) ?: false

        val records = dataList.map { raw ->
            RawRecord(
                id     = raw["id"]?.toString() ?: "",
                fields = raw,
            )
        }
        Result.success(records to hasMore)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Related records ───────────────────────────────────────────────────────

    suspend fun listRelatedRecords(
        parentModule : String,
        parentId     : String,
        relatedModule: String,
        perPage      : Int = 10,
    ): Result<List<RawRecord>> = try {
        val response = api.listRelatedRecords(
            parentModule  = parentModule,
            parentId      = parentId,
            relatedModule = relatedModule,
            perPage       = perPage,
        )

        @Suppress("UNCHECKED_CAST")
        val dataList = response
            ?.get("data") as? List<Map<String, Any?>> ?: emptyList()

        val records = dataList.map { raw ->
            RawRecord(
                id     = raw["id"]?.toString() ?: "",
                fields = raw,
            )
        }
        Result.success(records)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Create ────────────────────────────────────────────────────────────────

    suspend fun createRecord(
        module : String,
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

    // ── Update ────────────────────────────────────────────────────────────────

    suspend fun updateRecord(
        module : String,
        id     : String,
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

    // ── Get single ────────────────────────────────────────────────────────────

    suspend fun getRecord(
        module: String,
        id    : String,
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

    // ── Delete ────────────────────────────────────────────────────────────────

    suspend fun deleteRecord(
        module: String,
        id    : String,
    ): Result<Unit> = try {
        api.deleteRecord(module, id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}