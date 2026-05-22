package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Task
import com.pookie.octfis.data.remote.ZohoApiService

class TaskRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Task>()
    }

    private fun ZohoApiService.mapTask(z: com.pookie.octfis.data.remote.dto.ZohoTask) = Task(
        zohoId      = z.id,
        subject     = z.subject.orEmpty().ifEmpty { "(No Subject)" },
        dueDate     = z.dueDate.orEmpty().take(10),
        status      = z.status.orEmpty().ifEmpty { "Not Started" },
        priority    = z.priority.orEmpty().ifEmpty { "Normal" },
        description = z.description.orEmpty(),
        ownerName   = z.owner?.name.orEmpty(),
        ownerId     = z.owner?.id.orEmpty(),
        relatedTo   = z.whatId?.name.orEmpty(),
        contactName = z.whoId?.name.orEmpty(),
        remindAt    = "",  // FlexibleReminder? object, not used in UI
        closedTime  = z.closedTime.orEmpty(),
    )

    suspend fun getTasks(page: Int = 1): Result<Pair<List<Task>, Boolean>> = runCatching {
        val r = api.getTasks(page = page, perPage = 50)
        val tasks = r.data?.map { api.mapTask(it) } ?: emptyList()
        if (page == 1) cache.clear()
        cache.addAll(tasks)
        Pair(tasks, r.info?.moreRecords ?: false)
    }

    suspend fun getTaskById(zohoId: String): Result<Task> = runCatching {
        val z = api.getTaskById(zohoId).data?.firstOrNull()
            ?: error("Task not found: $zohoId")
        api.mapTask(z)
    }

    suspend fun createTask(
        subject    : String,
        dueDate    : String,
        status     : String,
        priority   : String,
        description: String,
        ownerId    : String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Subject", subject)
            if (dueDate.isNotBlank())     put("Due_Date",    dueDate)
            if (status.isNotBlank())      put("Status",      status)
            if (priority.isNotBlank())    put("Priority",    priority)
            if (description.isNotBlank()) put("Description", description)
            if (ownerId.isNotBlank())     put("Owner",       mapOf("id" to ownerId))
        }
        val r = api.createTask(mapOf("data" to listOf(record)))
        val result = r.data?.firstOrNull()
        if (result?.status == "success") result.details?.id ?: "created"
        else error(result?.message ?: "Create failed")
    }

    suspend fun updateTask(
        zohoId     : String,
        subject    : String,
        dueDate    : String,
        status     : String,
        priority   : String,
        description: String,
        ownerId    : String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            put("Subject",     subject)
            // FIX: guard with isNotBlank() — sending an empty string for a date
            // field causes Zoho to return "invalid data"
            if (dueDate.isNotBlank())     put("Due_Date",    dueDate)
            if (status.isNotBlank())      put("Status",      status)
            if (priority.isNotBlank())    put("Priority",    priority)
            if (description.isNotBlank()) put("Description", description)
            if (ownerId.isNotBlank())     put("Owner",       mapOf("id" to ownerId))
        }
        val r = api.updateTask(zohoId, mapOf("data" to listOf(record)))
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Update failed")
        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) cache[idx] = cache[idx].copy(
            subject = subject, dueDate = dueDate, status = status,
            priority = priority, description = description,
        )
    }

    suspend fun deleteTask(zohoId: String): Result<Unit> = runCatching {
        val r = api.deleteTask(zohoId)
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Delete failed")
        cache.removeAll { it.zohoId == zohoId }
    }
}