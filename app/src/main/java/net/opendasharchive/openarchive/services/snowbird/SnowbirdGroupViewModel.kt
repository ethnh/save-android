package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout

class SnowbirdGroupViewModel(private val repository: ISnowbirdGroupRepository) : BaseViewModel() {

    sealed class GroupState {
        data object Idle : GroupState()
        data object Loading : GroupState()
        data class SingleGroupSuccess(val group: SnowbirdGroup) : GroupState()
        data class MultiGroupSuccess(val groups: List<SnowbirdGroup>) : GroupState()
        data class Error(val error: SnowbirdError) : GroupState()
    }

    private val _groupState = MutableStateFlow<GroupState>(GroupState.Idle)
    val groupState: StateFlow<GroupState> = _groupState.asStateFlow()

    private val _currentGroup = MutableStateFlow<SnowbirdGroup?>(null)
    val currentGroup: StateFlow<SnowbirdGroup?> = _currentGroup.asStateFlow()

    fun setCurrentGroup(group: SnowbirdGroup) {
        _currentGroup.value = group
    }

    fun fetchGroup(groupKey: String) {
        viewModelScope.launch {
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "fetch_group") {
                    repository.fetchGroup(groupKey)
                }

                _groupState.value = when (result) {
                    is SnowbirdResult.Success -> GroupState.SingleGroupSuccess(result.value)
                    is SnowbirdResult.Failure -> GroupState.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _groupState.value = GroupState.Error(SnowbirdError.TimedOut)
            }
        }
    }

    fun fetchGroups(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "fetch_groups") {
                    repository.fetchGroups(forceRefresh)
                }

                _groupState.value = when (result) {
                    is SnowbirdResult.Success -> GroupState.MultiGroupSuccess(result.value)
                    is SnowbirdResult.Failure -> GroupState.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _groupState.value = GroupState.Error(SnowbirdError.TimedOut)
            }
        }
    }

    fun createGroup(groupName: String) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "create_group") {
                    repository.createGroup(groupName)
                }

                _groupState.value = when (result) {
                    is SnowbirdResult.Success -> GroupState.SingleGroupSuccess(result.value)
                    is SnowbirdResult.Failure -> GroupState.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _groupState.value = GroupState.Error(SnowbirdError.TimedOut)
            }
        }
    }

    fun joinGroup(uriString: String) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "join_group") {
                    repository.joinGroup(uriString)
                }

                _groupState.value = when (result) {
                    is SnowbirdResult.Success -> GroupState.SingleGroupSuccess(result.value)
                    is SnowbirdResult.Failure -> GroupState.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _groupState.value = GroupState.Error(SnowbirdError.TimedOut)
            }
        }
    }
}