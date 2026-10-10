package com.crisos.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crisos.app.CrisosApplication
import com.crisos.app.domain.model.SosMessage
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Message History and Received Alerts screens.
 */
class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CrisosApplication
    private val repository = app.sosRepository

    val allMessages: StateFlow<List<SosMessage>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sentMessages: StateFlow<List<SosMessage>> = repository.getSentMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivedMessages: StateFlow<List<SosMessage>> = repository.getReceivedMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMessages: StateFlow<List<SosMessage>> = repository.getPendingMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
