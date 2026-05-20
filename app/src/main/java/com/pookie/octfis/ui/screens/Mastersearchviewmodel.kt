package com.pookie.octfis.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.octfis.data.model.ActivityCall
import com.pookie.octfis.data.model.ActivityMeeting
import com.pookie.octfis.data.model.ActivityTask
import com.pookie.octfis.data.remote.ZohoServiceLocator
import com.pookie.octfis.data.repository.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Result model ──────────────────────────────────────────────────────────────

enum class SearchModule(val label: String) {
    ACCOUNT("Account"),
    CONTACT("Contact"),
    DEAL("Deal"),
    QUOTE("Quote"),
    TASK("Task"),
    MEETING("Meeting"),
    CALL("Call"),
}

data class SearchResult(
    val module  : SearchModule,
    val id      : String,   // zohoId for accounts/contacts/deals/quotes; raw id for activities
    val title   : String,
    val subtitle: String,
    val badge   : String = "",
)

data class MasterSearchUiState(
    val query      : String             = "",
    val results    : List<SearchResult> = emptyList(),
    val isSearching: Boolean            = false,
    val primed     : Boolean            = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
class MasterSearchViewModel : ViewModel() {

    private val api         = ZohoServiceLocator.getApiService()
    private val accountRepo = AccountRepository(api)
    private val contactRepo = ContactRepository(api)
    private val dealRepo    = DealRepository(api)
    private val quoteRepo   = QuoteRepository(api)
    private val actRepo     = ActivityRepository(api)

    private val _state = MutableStateFlow(MasterSearchUiState())
    val state: StateFlow<MasterSearchUiState> = _state.asStateFlow()

    // Activity module snapshots (no companion cache in ActivityRepository)
    private val taskSnap    = mutableListOf<ActivityTask>()
    private val meetingSnap = mutableListOf<ActivityMeeting>()
    private val callSnap    = mutableListOf<ActivityCall>()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(250)
                .distinctUntilChanged()
                .collect { runSearch(it) }
        }
        viewModelScope.launch { prime() }
    }

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        queryFlow.value = q
    }

    fun clear() {
        _state.update { it.copy(query = "", results = emptyList()) }
        queryFlow.value = ""
    }

    // ── Prime all module caches ───────────────────────────────────────────────

    private suspend fun prime() {
        _state.update { it.copy(isSearching = true) }
        runCatching { accountRepo.getAccounts() }
        runCatching { contactRepo.getContacts() }
        runCatching { dealRepo.getDeals() }
        runCatching { quoteRepo.getQuotes() }
        runCatching {
            val d = actRepo.getDashboardData().getOrThrow()
            taskSnap.clear();    taskSnap.addAll(d.allTasks)
            meetingSnap.clear(); meetingSnap.addAll(d.allMeetings)
            callSnap.clear();    callSnap.addAll(d.allCalls)
        }
        _state.update { it.copy(isSearching = false, primed = true) }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private fun runSearch(q: String) {
        if (q.isBlank()) {
            _state.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        val term = q.trim().lowercase()
        val results = mutableListOf<SearchResult>()

        // Accounts
        AccountRepository.cache.forEach { a ->
            if (matches(term, a.name, a.phone, a.website, a.industry,
                    a.billingCity, a.billingState, a.billingCountry,
                    a.accountOwner, a.gstin, a.accountNo)) {
                results += SearchResult(
                    module   = SearchModule.ACCOUNT,
                    id       = a.zohoId,
                    title    = a.name,
                    subtitle = listOf(a.phone, a.billingCity, a.industry)
                        .filter { it.isNotBlank() && it != "-None-" }.joinToString(" · "),
                    badge    = a.industry.takeIf { it.isNotBlank() && it != "-None-" }.orEmpty(),
                )
            }
        }

        // Contacts
        ContactRepository.cache.forEach { c ->
            if (matches(term, c.fullName, c.email, c.phone, c.mobile,
                    c.accountName, c.title, c.department,
                    c.mailingCity, c.mailingState)) {
                results += SearchResult(
                    module   = SearchModule.CONTACT,
                    id       = c.zohoId,
                    title    = c.fullName,
                    subtitle = listOf(c.email, c.accountName)
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    badge    = c.title.takeIf { it.isNotBlank() }.orEmpty(),
                )
            }
        }

        // Deals
        DealRepository.cache.forEach { d ->
            if (matches(term, d.dealName, d.accountName, d.contactName,
                    d.stage, d.amount, d.closingDate, d.type,
                    d.leadSource, d.dealOwner, d.description)) {
                results += SearchResult(
                    module   = SearchModule.DEAL,
                    id       = d.zohoId,
                    title    = d.dealName.ifEmpty { d.name },
                    subtitle = listOf(d.accountName,
                        d.amount.let { if (it.isNotBlank()) "₹$it" else "" })
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    badge    = d.stage.takeIf { it.isNotBlank() && it != "-None-" }.orEmpty(),
                )
            }
        }

        // Quotes
        QuoteRepository.cache.forEach { qu ->
            if (matches(term, qu.subject, qu.accountName, qu.contactName,
                    qu.dealName, qu.quoteStage,
                    "%.0f".format(qu.grandTotal), qu.description)) {
                results += SearchResult(
                    module   = SearchModule.QUOTE,
                    id       = qu.zohoId,
                    title    = qu.subject.ifEmpty { qu.name },
                    subtitle = listOf(qu.accountName, "₹${"%.0f".format(qu.grandTotal)}")
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    badge    = qu.quoteStage,
                )
            }
        }

        // Tasks
        taskSnap.forEach { t ->
            if (matches(term, t.subject, t.status, t.priority, t.dueDate)) {
                results += SearchResult(
                    module   = SearchModule.TASK,
                    id       = t.id,
                    title    = t.subject,
                    subtitle = "Due: ${t.dueDate.take(10).ifEmpty { "—" }}",
                    badge    = t.status,
                )
            }
        }

        // Meetings
        meetingSnap.forEach { m ->
            if (matches(term, m.title, m.startDateTime)) {
                results += SearchResult(
                    module   = SearchModule.MEETING,
                    id       = m.id,
                    title    = m.title,
                    subtitle = m.startDateTime.take(16).replace("T", " at "),
                )
            }
        }

        // Calls
        callSnap.forEach { c ->
            if (matches(term, c.subject, c.callType, c.status, c.startTime)) {
                results += SearchResult(
                    module   = SearchModule.CALL,
                    id       = c.id,
                    title    = c.subject,
                    subtitle = "${c.callType} · ${c.startTime.take(10)}",
                    badge    = c.status,
                )
            }
        }

        _state.update { it.copy(results = results) }
    }

    private fun matches(term: String, vararg fields: String) =
        fields.any { it.contains(term, ignoreCase = true) }
}