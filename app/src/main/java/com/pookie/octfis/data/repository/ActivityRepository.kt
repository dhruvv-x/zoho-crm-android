package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.ActivityCall
import com.pookie.octfis.data.model.ActivityMeeting
import com.pookie.octfis.data.model.ActivityTask
import com.pookie.octfis.data.remote.ZohoApiService
import java.time.LocalDate

data class DashboardData(
    // Today tab — filtered by today's date + current user
    val todayTasks    : List<ActivityTask>,
    val todayMeetings : List<ActivityMeeting>,
    val todayCalls    : List<ActivityCall>,
    // Calls / Meetings / Tasks tabs — all records, no filter
    val allTasks      : List<ActivityTask>,
    val allMeetings   : List<ActivityMeeting>,
    val allCalls      : List<ActivityCall>,
)

class ActivityRepository(private val api: ZohoApiService) {

    suspend fun getDashboardData(): Result<DashboardData> = runCatching {
        val today         = LocalDate.now().toString()
        val currentUserId = api.getUsers("CurrentUser").users?.firstOrNull()?.id.orEmpty()

        // ── Tasks — fetch once, split in memory ───────────────────────────
        val allTasksRaw = api.getTasks().data ?: emptyList()
        val allTasks = allTasksRaw.map {
            ActivityTask(
                id       = it.id,
                subject  = it.subject.orEmpty().ifEmpty { "(No Subject)" },
                dueDate  = it.dueDate.orEmpty().take(10),
                status   = it.status.orEmpty(),
                priority = it.priority.orEmpty(),
            )
        }
        val todayTasks = allTasksRaw
            .filter { it.owner?.id == currentUserId && it.dueDate?.take(10) == today }
            .map { raw -> allTasks.first { it.id == raw.id } }

        // ── Meetings — fetch once, split in memory ────────────────────────
        val allMeetingsRaw = api.getEvents().data ?: emptyList()
        val allMeetings = allMeetingsRaw.map {
            ActivityMeeting(
                id            = it.id,
                title         = it.title.orEmpty().ifEmpty { "(No Title)" },
                startDateTime = it.startDateTime.orEmpty(),
                endDateTime   = it.endDateTime.orEmpty(),
            )
        }
        val todayMeetings = allMeetingsRaw
            .filter { it.owner?.id == currentUserId && it.startDateTime?.take(10) == today }
            .map { raw -> allMeetings.first { it.id == raw.id } }

        // ── Calls — fetch once, split in memory ───────────────────────────
        val allCallsRaw = api.getCalls().data ?: emptyList()
        val allCalls = allCallsRaw.map {
            ActivityCall(
                id        = it.id,
                subject   = it.subject.orEmpty().ifEmpty { "(No Subject)" },
                startTime = it.callStartTime.orEmpty(),
                duration  = it.duration.orEmpty(),
                callType  = it.callType.orEmpty(),
                status    = it.status.orEmpty(),
            )
        }
        val todayCalls = allCallsRaw
            .filter { it.owner?.id == currentUserId && it.callStartTime?.take(10) == today }
            .map { raw -> allCalls.first { it.id == raw.id } }

        DashboardData(
            todayTasks    = todayTasks,
            todayMeetings = todayMeetings,
            todayCalls    = todayCalls,
            allTasks      = allTasks,
            allMeetings   = allMeetings,
            allCalls      = allCalls,
        )
    }
}