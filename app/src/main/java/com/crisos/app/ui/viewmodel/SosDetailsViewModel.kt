package com.crisos.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.SosMessage
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the SOS Details screen.
 */
class SosDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val repository = app.sosRepository

    private val _messageId = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val message: StateFlow<SosMessage?> = _messageId
        .filter { it.isNotBlank() }
        .flatMapLatest { id -> repository.getMessageById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadMessage(messageId: String) {
        _messageId.value = messageId
    }
}
