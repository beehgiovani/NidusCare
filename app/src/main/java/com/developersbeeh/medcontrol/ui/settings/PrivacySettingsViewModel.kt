package com.developersbeeh.medcontrol.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.developersbeeh.medcontrol.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _dataCollectionEnabled = MutableLiveData<Boolean>()
    val dataCollectionEnabled: LiveData<Boolean> = _dataCollectionEnabled

    init {
        _dataCollectionEnabled.value = userPreferences.getDataCollectionEnabled()
    }

    fun setDataCollectionEnabled(enabled: Boolean) {
        userPreferences.setDataCollectionEnabled(enabled)
        _dataCollectionEnabled.value = enabled
    }
}
