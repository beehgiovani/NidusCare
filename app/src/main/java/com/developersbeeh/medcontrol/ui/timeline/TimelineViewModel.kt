package com.developersbeeh.medcontrol.ui.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.TimelineEvent
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.util.TimelineIconMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

private const val TAG = "TimelineViewModel"

enum class TimelineFilter {
    ALL, DOSE, NOTE, ACTIVITY, INSIGHT, WELLBEING
}

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TimelineFilter.ALL)
    private val _dependentId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelinePagerFlow: Flow<PagingData<TimelineListItem>> =
        combine(_dependentId.filterNotNull(), _filter) { id, filter ->
            Pair(id, filter)
        }.flatMapLatest { (id, filter) ->
            firestoreRepository.getTimelinePager(id, filter).flow
        }.map { pagingData ->
            pagingData.map { event ->
                event.toTimelineLogItem()
            }
        }.map { pagingData ->
            pagingData.insertSeparators { before, after ->
                if (after == null) {
                    return@insertSeparators null
                }
                val beforeDate = (before as? TimelineListItem.LogItem)?.log?.timestamp?.toLocalDate()
                val afterDate = (after as? TimelineListItem.LogItem)?.log?.timestamp?.toLocalDate()

                if (beforeDate != afterDate) {
                    TimelineListItem.HeaderItem(afterDate!!)
                } else {
                    null
                }
            }
        }.cachedIn(viewModelScope)

    fun initialize(dependentId: String) {
        if (_dependentId.value == dependentId) return
        _dependentId.value = dependentId
    }

    fun setFilter(filter: TimelineFilter) {
        _filter.value = filter
    }

    private fun TimelineEvent.toTimelineLogItem(): TimelineListItem.LogItem {
        val category = when (this.type) {
            "DOSE" -> TimelineItemCategory.DOSE
            "NOTE" -> TimelineItemCategory.NOTE
            "ACTIVITY" -> TimelineItemCategory.ACTIVITY
            "INSIGHT" -> TimelineItemCategory.INSIGHT
            "WELLBEING" -> TimelineItemCategory.WELLBEING
            else -> {
                Log.w(TAG, "Tipo de evento da timeline desconhecido: '${this.type}'. Classificando como ACTIVITY.")
                TimelineItemCategory.ACTIVITY
            }
        }

        return TimelineListItem.LogItem(
            TimelineLogItem(
                id = this.id,
                timestamp = this.getLocalDateTime(),
                description = this.description,
                author = this.authorName,
                iconRes = TimelineIconMapper.getIconRes(this.icon),
                iconTintRes = TimelineIconMapper.getColorRes(this.type),
                category = category,
                noteType = try { HealthNoteType.valueOf(this.icon) } catch (e: Exception) { null }
            )
        )
    }
}